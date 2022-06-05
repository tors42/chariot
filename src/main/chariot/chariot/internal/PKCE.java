package chariot.internal;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.Random;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpServer;

import chariot.Client.Scope;
import chariot.api.Account.UriAndToken;
import chariot.api.Account.UriAndTokenExchange;
import chariot.model.TokenResult;

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

    public static UriAndToken initiateAuthorizationFlow(
            Set<Scope> scopes,
            String lichessUri,
            Function<Map<String,String>, TokenResult> apiTokenLookup) throws Exception {

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

                    var successPage = """
                        <html>
                        <body>
                        <h1>Success, you may close this page</h1>
                        <p>If you later want to revoke the token, you can do so from
                        <a href="%s">Security</a> in Preferences of your Lichess account.</p>
                        </body>
                        </html>"""
                        .formatted(lichessUri + "/account/security");

                    var responseBytes = successPage.getBytes();
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    exchange.getResponseBody().write(responseBytes);
                    exchange.close();

                    cf.complete(new StateAndCode(instate, code));
                });

        httpServer.start();

        var urlAndTokenExchange = initiateAuthorizationFlowCustom(scopes, lichessUri, apiTokenLookup, redirectUri);

        return new UriAndToken(urlAndTokenExchange.url(), () -> {
            try {
                var stateAndCode = cf.get(2, TimeUnit.MINUTES);
                return urlAndTokenExchange.token(stateAndCode.code(), stateAndCode.state()).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                httpServer.stop(0);
            }
        });
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

}
