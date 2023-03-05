package chariot.internal;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

import com.sun.net.httpserver.HttpServer;

import chariot.Client;
import chariot.Client.*;
import chariot.api.Account.*;
import chariot.model.TokenResult;
import chariot.internal.impl.AccountImpl;

public class PKCE {

    record CodeVerifier(String code_verifier) {};
    record Challenge(String code_challenge, String code_challenge_method) {}

    record Request(Challenge challenge, String response_type, String client_id, String redirect_uri, String scope, String state) {

        public Map<String, String> toMap() {
            return Map.of(
                    "code_challenge_method", challenge.code_challenge_method(),
                    "code_challenge", challenge.code_challenge(),
                    "response_type", response_type(),
                    "client_id", client_id(),
                    "redirect_uri", redirect_uri(),
                    "scope", scope(),
                    "state", state()
                    );
        }
    }

    public static String successPage(String lichessUri) {
        return """
            <html>
            <body>
            <h1>Success, you may close this page</h1>
            <p>If you later want to revoke the token, you can do so from
            <a href="%s">Security</a> in Preferences of your Lichess account.</p>
            </body>
            </html>"""
            .formatted(lichessUri + "/account/security");
    }

    @SuppressWarnings(value = {"deprecation"})
    public static UriAndTokenExchange initiateAuthorizationFlowCustom(
            Set<Scope> scopes,
            String lichessUri,
            Function<Map<String,String>, TokenResult> apiTokenLookup,
            URI redirectUri
            ) throws Exception {

        var moduleName = System.getProperty("jdk.module.main", Optional.ofNullable(PKCE.class.getModule().getName()).orElse("chariot"));
        var oauthUri = lichessUri + "/oauth";

        var code_verifier = generateRandomCodeVerifier();
        var oauthRequest = new Request(
                generateChallenge(code_verifier),
                "code",
                moduleName,
                redirectUri.toString(),
                scopes.stream().map(Scope::asString).collect(Collectors.joining(" ")),
                generateRandomState()
                );

        var paramString = Util.urlEncode(oauthRequest.toMap());
        var authUrlWithParameters = oauthUri + "?" + paramString;

        var frontChannelUrl = URI.create(authUrlWithParameters);


        var uriAndToken = new UriAndTokenExchange() {
            public URI url() {
                return frontChannelUrl;
            }

            public Supplier<Supplier<char[]>> token(String code, String state) {
                if ( ! oauthRequest.state().equals(state)) {
                    throw new RuntimeException("Wrong State!");
                }

                if (code == null) {
                    throw new RuntimeException("Authorization Failed");
                }

                var tokenParameters = Map.of(
                        "code_verifier", code_verifier.code_verifier(),
                        "grant_type", "authorization_code",
                        "code", code,
                        "redirect_uri", oauthRequest.redirect_uri(),
                        "client_id", oauthRequest.client_id()
                        );

                var tokenResult = apiTokenLookup.apply(tokenParameters);

                if (tokenResult instanceof TokenResult.AccessToken apiToken) {
                    var enc = Crypt.encrypt(apiToken.access_token().toCharArray());
                    return () -> () -> Crypt.decrypt(enc.data(), enc.key());
                } else {
                    var message = "Authorization Failed";
                    if (tokenResult instanceof TokenResult.Error error) {
                        message = error.error() + " - " + error.error_description();
                    }
                    throw new RuntimeException(message);
                }
            }
        };

        return uriAndToken;
    }


    @SuppressWarnings(value = {"deprecation"})
    public static UriAndToken initiateAuthorizationFlow(
            Set<Scope> scopes,
            String lichessUri,
            Function<Map<String,String>, TokenResult> apiTokenLookup,
            Duration timeout,
            String successPage) throws Exception {

        var loopbackAddress = InetAddress.getLoopbackAddress();
        var local = new InetSocketAddress(loopbackAddress, 0);
        var httpServer = HttpServer.create(local, 0);
        var redirectHost = loopbackAddress.getHostAddress();
        var redirectPort = httpServer.getAddress().getPort();

        URI redirectUri = URI.create("http://" + redirectHost + ":" + redirectPort + "/");

        record StateAndCode(String state, String code) {}

        var cf = new CompletableFuture<StateAndCode>();
        httpServer.createContext("/",
                (exchange) -> {
                    httpServer.removeContext("/");

                    var requestUri = exchange.getRequestURI();
                    var query = requestUri.getQuery();
                    var inparams = Arrays.stream(query.split("&"))
                        .collect(Collectors.toMap(
                                    s -> s.split("=")[0],
                                    s -> s.split("=")[1]
                                    ));
                    var code = inparams.get("code");
                    var instate = inparams.get("state");

                    if (code == null) {
                        exchange.sendResponseHeaders(503, -1);
                        exchange.close();
                        httpServer.stop(0);
                        cf.completeExceptionally(new Exception("Authorization Failed"));
                        return;
                    }

                    var responseBytes = successPage.getBytes();
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    exchange.getResponseBody().write(responseBytes);
                    exchange.close();

                    cf.complete(new StateAndCode(instate, code));
                });

        httpServer.start();

        var urlAndTokenExchange = initiateAuthorizationFlowCustom(scopes, lichessUri, apiTokenLookup, redirectUri);
        var uri = urlAndTokenExchange.url();

        Supplier<Supplier<char[]>> tokenSupplier = () -> {
            try {
                var stateAndCode = cf.get(timeout.toMinutes(), TimeUnit.MINUTES);
                var supplier = urlAndTokenExchange.token(stateAndCode.code(), stateAndCode.state());
                return supplier.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                httpServer.stop(0);
            }
        };

        var uriAndToken = new UriAndToken(uri, tokenSupplier);

        return uriAndToken;
    }

    static CodeVerifier generateRandomCodeVerifier() {
        var bytes = randomBytes(32);
        var code_verifier = encodeBytes(bytes);
        return new CodeVerifier(code_verifier);
    }

    static byte[] randomBytes(int num) {
        var bytes = new byte[num];
        new Random().nextBytes(bytes);
        return bytes;
    }

    static Challenge generateChallenge(CodeVerifier code_verifier) {
        var asciiBytes = code_verifier.code_verifier().getBytes(StandardCharsets.US_ASCII);
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException("SHA-256 algorithm not available", nsae);
        }
        var s256bytes = md.digest(asciiBytes);
        var code_challenge = encodeBytes(s256bytes);
        return new Challenge(code_challenge, "S256");
    }

    static String generateRandomState() {
        var bytes = randomBytes(16);
        return encodeBytes(bytes).substring(0, 8);
    }

    static String encodeBytes(byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes)
            .replaceAll(  "=",  "")
            .replaceAll("\\+", "-")
            .replaceAll("\\/", "_");
    }

    public static AuthResult pkceAuth(Client client, Consumer<URI> uriHandler, Consumer<PkceConfig> pkce) {
        if (! (client instanceof chariot.internal.DefaultClient dc)) return new Client.AuthFail("Internal Error");

        record Custom(URI redirectUri, Supplier<Client.CodeAndState> codeAndState) {}
        record Data(Optional<Set<Scope>> scope, Optional<Duration> timeout, Optional<String> htmlSuccess, Optional<String> usernameHint, Optional<Custom> custom) {}

        final class Builder implements PkceConfig {
            Data data = new Data(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

            @Override
            public PkceConfig scope(Scope... scopes) {
                var updatedScopes = Stream.concat(
                        data.scope().orElse(Set.of()).stream(),
                        Arrays.stream(scopes))
                    .collect(Collectors.toSet());
                data = new Data(Optional.of(updatedScopes), data.timeout(), data.htmlSuccess(), data.usernameHint(), data.custom());
                return this;
            }

            @Override
            public PkceConfig timeout(Duration duration) {
                data = new Data(data.scope(), Optional.of(duration), data.htmlSuccess(), data.usernameHint(), data.custom());
                return this;
            }

            @Override
            public PkceConfig htmlSuccess(String html) {
                data = new Data(data.scope(), data.timeout(), Optional.of(html), data.usernameHint(), data.custom());
                return this;
            }

            @Override
            public PkceConfig customRedirect(URI redirectUri, Supplier<CodeAndState> codeAndState) {
                data = new Data(data.scope(), data.timeout(), data.htmlSuccess(), data.usernameHint(), Optional.of(new Custom(redirectUri, codeAndState)));
                return this;
            }
        }

        var builder = new Builder();
        pkce.accept(builder);

        Data data = builder.data;

        Set<Scope> scopes = data.scope().orElse(Set.of());
        Duration timeout = data.timeout().orElse(Duration.ofMinutes(2));
        String html = data.htmlSuccess().orElse(PKCE.successPage(dc.config().servers().api().get()));
        //String usernameHint = data.usernameHint().orElse(null);

        if (data.custom().isEmpty()) {
            try {
                var uriAndToken = PKCE.initiateAuthorizationFlow(
                        scopes,
                        dc.config().servers().api().get(),
                        map -> ((AccountImpl)dc.account()).token(map),
                        timeout,
                        html);

                uriHandler.accept(uriAndToken.url());
                return new AuthOk(client.withToken(uriAndToken.token().get()));
            } catch (Exception e) {
                return new AuthFail(e.getMessage());
            }
        } else {
            URI redirectUri = data.custom().get().redirectUri();
            Supplier<CodeAndState> codeAndStateSupplier = data.custom().get().codeAndState();

            try {
                var exchange = PKCE.initiateAuthorizationFlowCustom(scopes, dc.config().servers().api().get(), map -> ((AccountImpl)dc.account()).token(map), redirectUri);
                @SuppressWarnings(value = {"deprecation"})
                URI uri = exchange.url();
                uriHandler.accept(uri);
                var codeAndState = codeAndStateSupplier.get();
                @SuppressWarnings(value = {"deprecation"})
                var supplier = exchange.token(codeAndState.code(), codeAndState.state());
                return new AuthOk(client.withToken(supplier.get()));
            } catch (Exception e) {
                return new AuthFail(e.getMessage());
            }
        }
    }
}
