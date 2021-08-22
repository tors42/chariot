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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpServer;

import chariot.Client.Scope;
import chariot.api.Account.UriAndToken;
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

    public static UriAndToken initiateAuthorizationFlow(
            Set<Scope> scopes,
            String lichessUri,
            Function<Map<String,String>, TokenResult> apiTokenLookup) throws Exception {

        var moduleName = System.getProperty("jdk.module.main", Optional.ofNullable(PKCE.class.getModule().getName()).orElse("chariot"));

        var loopbackAddress = InetAddress.getLoopbackAddress();
        var local = new InetSocketAddress(loopbackAddress, 0);
        var httpServer = HttpServer.create(local, 0);
        var redirectHost = loopbackAddress.getHostAddress();
        var redirectPort = httpServer.getAddress().getPort();

        var oauthUri = lichessUri + "/oauth";


        var code_verifier = generateRandomCodeVerifier();
        var oauthRequest = new Request(
                generateChallenge(code_verifier),
                "code",
                moduleName,
                "http://" + redirectHost + ":" + redirectPort,
                scopes.stream().map(Scope::asString).collect(Collectors.joining(",")),
                generateRandomState()
                );

        var paramString = Util.urlEncode(oauthRequest.toMap());

        var authUrlWithParameters = oauthUri + "?" + paramString;

        var frontChannelUrl = URI.create(authUrlWithParameters);

        var cf = new CompletableFuture<Supplier<char[]>>();
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

                    if ( ! oauthRequest.state().equals(instate)) {
                        System.out.format("Wrong state [%s]", instate);
                        cf.completeExceptionally(new Exception("Authorization Failed"));
                        exchange.sendResponseHeaders(503, -1);
                        return;
                    } else {
                        // This should be configurable/optional/better-default?
                        var successPage = """
                            <html>
                                <body>
                                    <h1>Success, you may close this page</h1>
                                    <p>If you later want to revoke this token, you can do so from <a href="%s">Security</a> in Preferences of your Lichess account.</p>
                                </body>
                            </html>"""
                            .formatted(lichessUri + "/account/security");

                        var responseBytes = successPage.getBytes();
                        exchange.sendResponseHeaders(200, responseBytes.length);
                        exchange.getResponseBody().write(responseBytes);
                    }

                    var tokenParameters = Map.of(
                            "code_verifier", code_verifier.code_verifier(),
                            "grant_type", "authorization_code",
                            "code", code,
                            "redirect_uri", oauthRequest.redirect_uri(), // we don't expect any more requests, value used for server side verification
                            "client_id", oauthRequest.client_id()
                            );

                    var tokenResult = apiTokenLookup.apply(tokenParameters);

                    if (tokenResult instanceof TokenResult.AccessToken apiToken) {
                        var enc = Crypt.encrypt(apiToken.access_token().toCharArray());
                        cf.complete(() -> Crypt.decrypt(enc.data(), enc.key()));
                    } else {
                        var message = "Authorization Failed";
                        if (tokenResult instanceof TokenResult.Error error) {
                            message = error.error() + " - " + error.error_description();
                        }
                        cf.completeExceptionally(new Exception(message));
                    }
                });

        httpServer.start();

        var result = new UriAndToken(frontChannelUrl, new Supplier<Supplier<char[]>>() {
            @Override
            public Supplier<char[]> get() {
                try {
                    var supplier = cf.get(2, TimeUnit.MINUTES);
                    return supplier;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    httpServer.stop(0);
                }
            }
        });

        return result;
    }

    static CodeVerifier generateRandomCodeVerifier() {
        var bytes = randomBytes(32);
        var code_verifier = encodeBytes(bytes);
        return new CodeVerifier(code_verifier);
    }

    static byte[] randomBytes(int num) {
        var bytes = new byte[num];
        RandomGenerator.getDefault().nextBytes(bytes);
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
