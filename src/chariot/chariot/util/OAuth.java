package chariot.util;

import module java.base;
import module jdk.httpserver;
import module chariot;
import chariot.internal.Util;
import chariot.internal.Util.MapBuilder;

public interface OAuth {

    static One<String> lichessAuthorizationCodeFlowPKCE(Consumer<URI> visitURI, Consumer<Client.PkceConfig> pkce) {
        return _lichessAuthorizationCodeFlowPKCE(visitURI, pkce, Client.basic());
    }

    static One<String> twitchImplicitGrantFlow(Consumer<URI> visitURI, Consumer<TwitchParams> params) {
        return _twitchImplicitGrantFlow(visitURI, params);
    }

    static One<String> twitchClientCredentials(Consumer<TwitchCredentialsParams> params) {
        return _twitchClientCredentials(params);
    }

    static String generateCodeVerifier() { return encodeBytes(randomBytes(32)); }
    static String generateState() { return encodeBytes(randomBytes(16)).substring(0, 8); }
    static String generateCodeChallenge(String code_verifier) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException("SHA-256 algorithm not available", nsae);
        }
        return encodeBytes(md.digest(code_verifier.getBytes(StandardCharsets.US_ASCII)));
    }

    static String urlEncode(Map<String, ?> map) { return Util.urlEncode(map); }

    static InetSocketAddress uriToInetSocketAddress(URI uri) {
        int port = uri.getPort() != -1
            ? uri.getPort()
            : "http".equalsIgnoreCase(uri.getScheme()) ? 80 : 443;
        return new InetSocketAddress(uri.getHost(), port);
    }

    static URI inetSocketAddressToURI(InetSocketAddress inetSocketAddress, String path, boolean https) {
        String schemePart = https ? "https" : "http";
        String hostPart = inetSocketAddress.getHostString();
        int defaultPort = https ? 443 : 80;
        int port = inetSocketAddress.getPort();
        String portPart = port == defaultPort ? "" : ":%d".formatted(port);
        return URI.create("%s://%s%s/%s".formatted(schemePart, hostPart, portPart, path)).normalize();
    }

    sealed interface Bind {
        static Bind any() { return new Any(); }
        static Bind explicit(URI bind) { return new Explicit(bind, Opt.empty()); }
        static Bind explicit(URI bind, KeyStore keyStore, char[] keyStorePass) {
            return new Explicit(bind, Opt.of(new KeyStoreAndPass(keyStore, keyStorePass.clone())));
        }
    }

    record Any() implements Bind {}
    record Explicit(URI bind, Opt<KeyStoreAndPass> https) implements Bind {
        public Explicit {
            bind = bind.normalize();
            if (bind.getPath().equals("")) {
                bind = bind.resolve("/");
            }
        }
    }
    record KeyStoreAndPass(KeyStore keyStore, char[] pass) {}

    interface Params<T> {
        T bindExplicit(URI bindURI);
        T bindExplicit(URI bindURI, KeyStore keyStore, char[] pass);
        T timeout(Duration timeout);
        T successHtml(String html);
    }

    interface ScopeParams<T> {
        T scopes(String... scopes);
    }

    interface TwitchParams extends Params<TwitchParams>, ScopeParams<TwitchParams> {
        TwitchParams registeredApp(String client_id, String redirect_uri);
        TwitchParams noCache();
    }

    interface TwitchCredentialsParams {
        TwitchCredentialsParams registeredAppWithSecret(String client_id, String client_secret);
    }

    static One<String> lichessAuthorizationCodeFlowPKCE(Consumer<URI> uriHandler, Consumer<Client.PkceConfig> pkce, Client client) {
        return _lichessAuthorizationCodeFlowPKCE(uriHandler, pkce, client);
    }
    static One<String> _lichessAuthorizationCodeFlowPKCE(Consumer<URI> uriHandler, Consumer<Client.PkceConfig> pkce, Client client) {
        AtomicReference<Supplier<Client.CodeAndState>> codeAndStateSupplier = new AtomicReference<>();
        @SuppressWarnings("unchecked")
        var pkceMap = MapBuilder.of(Client.PkceConfig.class)
            .rename("appName", "client_id")
            .addCustomHandler("scope", (args, map) -> map.put("scopes",
                        ((Set<?>) args[0]).toArray(Client.Scope[]::new)))
            .addCustomHandler("customRedirect", (args, map) -> {
                map.put("remote_uri", args[0]);
                codeAndStateSupplier.set((Supplier<Client.CodeAndState>)args[1]);
            })
            .addCustomHandler("bindURI", (args, map) -> {
                map.put("bindURI", args[0]);
                if (args.length == 3) {
                    map.put("keystore", args[1]);
                    map.put("pass", args[2]);
                }
            })
            .toMap(pkce);

        String code_verifier = OAuth.generateCodeVerifier();
        String code_challenge = OAuth.generateCodeChallenge(code_verifier);
        String state = OAuth.generateState();
        String client_id = pkceMap.get("client_id") instanceof String value
            ? value : System.getProperty("jdk.module.main", "chariot");
        Duration timeout = pkceMap.get("timeout") instanceof Duration value
            ? value : Duration.ofMinutes(2);

        Optional<URI> redirectURI = pkceMap.get("redirectURI") instanceof URI value
            ? Optional.of(value) : Optional.empty();
        Optional<URI> bindURI = pkceMap.get("bindURI") instanceof URI value
            ? Optional.of(value) : Optional.empty();
        Optional<KeyStore> keystore = pkceMap.get("keystore") instanceof KeyStore value
            ? Optional.of(value) : Optional.empty();
        Optional<char[]> pass = pkceMap.get("pass") instanceof char[] value
            ? Optional.of(value) : Optional.empty();
        Set<Client.Scope> scopes = pkceMap.get("scopes") instanceof Client.Scope[] value
            ? Arrays.stream(value).collect(Collectors.toSet()) : Set.of();
        String htmlSuccess = (String) pkceMap.get("htmlSuccess");
        String username = (String) pkceMap.get("username");

        OAuth.Bind bind = bindURI
            .map(uri -> keystore.isPresent() && pass.isPresent()
                    ? OAuth.Bind.explicit(uri, keystore.get(), pass.get())
                    : OAuth.Bind.explicit(uri))
            .orElseGet(OAuth.Bind::any);

        Function<URI, URI> initialRequestHandler = (URI boundUri) -> {
            URI redirect_uri = redirectURI.orElse(boundUri);
            Consumer<OAuthApi.Params> params = p -> p
                    .redirect_uri(redirect_uri)
                    .scope(scopes)
                    .client_id(client_id)
                    .code_challenge(code_challenge)
                    .state(state);
            if (username != null) {
                params = params.andThen(after -> after.username(username));
            }
            URI authorizationURI = client.oauth().requestAuthorizationCodeURI(params);
            uriHandler.accept(authorizationURI);
            return redirect_uri;
        };

        Function<Map<String,String>, One<String>> initialResponseMapper = queryParams ->
            queryParams.get("code") instanceof String codeParam
            && queryParams.get("state") instanceof String stateParam
            && stateParam.equals(state)
            ? One.entry(codeParam) : One.fail("Missing code and/or mismatched state");

        BiFunction<URI, String, One<AccessToken>> finalRequestHandler = (URI redirect_uri, String codeOrNull) ->
            Optional.ofNullable(codeOrNull).map(
                    code -> client.oauth().obtainAccessToken(p -> p
                        .redirect_uri(redirect_uri)
                        .code(code)
                        .client_id(client_id)
                        .code_verifier(code_verifier)))
            .orElseGet(() -> One.fail("Missing code and/or invalid state parameter"));

        if (pkceMap.get("remote_uri") instanceof URI remoteURI) {
            if (! (codeAndStateSupplier.get() instanceof Supplier<Client.CodeAndState> supplier)) {
                return One.fail("Missing code and state supplier");
            }
            try {
                Client.CodeAndState codeAndState = supplier.get();
                return finalRequestHandler.apply(remoteURI, codeAndState.code())
                    .mapOne(AccessToken::access_token);
            } catch (Exception e) {
                return One.fail(e.getMessage());
            }
        }
        Consumer<OAuth.ManualParams> consumer = p -> p.timeout(timeout);
        if (htmlSuccess != null) {
            consumer = consumer.andThen(p -> p.successHtml(htmlSuccess));
        }
        consumer = switch(bind) {
            case OAuth.Any() -> consumer;
            case OAuth.Explicit(URI uri, Some(OAuth.KeyStoreAndPass(var ks, var ksPass))) -> consumer.andThen(p -> p.bindExplicit(uri, ks, ksPass));
            case OAuth.Explicit(URI uri, _) -> consumer.andThen(p -> p.bindExplicit(uri));
        };
        return OAuth.manualFlow(
                consumer,
                initialRequestHandler,
                initialResponseMapper,
                finalRequestHandler)
            .mapOne(AccessToken::access_token);
    }

    static One<String> _twitchImplicitGrantFlow(Consumer<URI> uriConsumer, Consumer<TwitchParams> params) {
        Map<String, Object> paramMap = MapBuilder.of(TwitchParams.class)
            .addCustomHandler("noCache", (args, map) -> map.put("noCache", true))
            .addCustomHandler("registeredApp", (args, map) -> {
                map.put("client_id", args[0]);
                map.put("redirect_uri", args[1]);
            })
            .addCustomHandler("bindExplicit", (args, map) -> map.put("bind",
                args.length == 1
                ? Bind.explicit((URI) args[0])
                : Bind.explicit((URI) args[0],(KeyStore)args[1], (char[])args[2])
                ))
            .toMap(params);

        String client_id = (String) paramMap.get("client_id");
        if (client_id == null) return One.fail("Missing client_id");

        Preferences prefs = paramMap.containsKey("noCache")
            ? null
            : Preferences.userRoot().node("chariot.twitch");

        if (prefs != null) {
            String cachedToken = prefs.get(client_id, null);
            if (cachedToken != null) {
                Client twitchOAuth = Client.basic(c -> c.api("https://id.twitch.tv/oauth2"))
                    .withToken(cachedToken);
                var validateEndpoint = twitchOAuth.custom().of(Function.identity())
                    .path("/validate").toOne();
                var res = validateEndpoint.request(p -> {});
                if (res instanceof Some) {
                    return One.entry(cachedToken);
                } else {
                    prefs.remove(client_id);
                    try { prefs.flush(); } catch (BackingStoreException _) {}
                }
            }
        }

        String redirect_uri = (String) paramMap.get("redirect_uri");
        if (redirect_uri == null) return One.fail("Missing redirect_uri");

        Bind bind = (Bind) paramMap.getOrDefault("bind", Bind.explicit(URI.create(redirect_uri)));
        Duration timeout = (Duration) paramMap.getOrDefault("timeout", Duration.ofMinutes(1));
        String scope = Arrays.stream((String[]) paramMap.getOrDefault("scopes", new String[]{""}))
            .collect(Collectors.joining(" "));
        String successHtml = (String) paramMap.getOrDefault("successHtml",
                "<html><body>Success</body></html>");

        String state = OAuth.generateState();

        Consumer<ManualParams> consumer = p -> p
            .remapFragmentAsQuery()
            .timeout(timeout)
            .successHtml(successHtml);
        consumer = switch(bind) {
            case Any() -> consumer;
            case Explicit(URI bindURI, Some(var ks)) -> consumer.andThen(p -> p.bindExplicit(bindURI, ks.keyStore(), ks.pass()));
            case Explicit(URI bindURI, _) -> consumer.andThen(p -> p.bindExplicit(bindURI));
        };

        return manualFlow(consumer,
                _ -> {
                    var map = Map.of("response_type", "token",
                                     "force_verify",  "true",
                                     "client_id",     client_id,
                                     "redirect_uri",  redirect_uri,
                                     "scope",         scope,
                                     "state",         state);
                    URI visitURI = URI.create("https://id.twitch.tv/oauth2/authorize?"
                            + Util.urlEncode(map));
                    uriConsumer.accept(visitURI);
                    return map;
                },
                queryParams -> {
                    if (! (queryParams.get("access_token") instanceof String access_token)) {
                        String error = queryParams.getOrDefault("error", "");
                        String errorDescription = queryParams.getOrDefault("error_description", "");
                        return One.fail(String.join(" - ", "Missing access_token", error, errorDescription));
                    }
                    if (! (queryParams.get("state") instanceof String paramState))
                        return One.fail("Missing state");
                    if (! state.equals(paramState))
                        return One.fail("state param mismatch");
                    return One.entry(access_token);
                },
                (var _, var token) -> {
                    if (prefs != null) {
                        prefs.put(client_id, token);
                        try { prefs.flush(); } catch (BackingStoreException _) {}
                    }
                    return One.entry(token);
                });
    }

    record Credentials(String access_token) {}
    static One<String> _twitchClientCredentials(Consumer<TwitchCredentialsParams> params) {
        Map<String, Object> paramMap = MapBuilder.of(TwitchCredentialsParams.class)
            .add("grant_type", "client_credentials")
            .addCustomHandler("registeredAppWithSecret", (args, map) -> {
                map.put("client_id", args[0]);
                map.put("client_secret", args[1]);
            })
        .toMap(params);
        if (paramMap.get("client_id") == null) return One.fail("Missing client_id");
        if (paramMap.get("client_secret") == null) return One.fail("Missing client_secret");
        Client twitchOAuth = Client.basic(c -> c.api("https://id.twitch.tv/oauth2"));
        var tokenEndpoint = twitchOAuth.custom().of(Credentials.class)
            .path("/token").post("application/x-www-form-urlencoded").toOne();
        return tokenEndpoint.request(p -> p.body(paramMap))
            .mapOne(Credentials::access_token);
    }

    interface ManualParams extends Params<ManualParams> {
        ManualParams remapFragmentAsQuery(boolean remap);
        default ManualParams remapFragmentAsQuery() {
            return remapFragmentAsQuery(true);
        }
    }

    static <INITIAL_DATA, CODE, TOKEN>
    One<TOKEN> manualFlow(
            Consumer<ManualParams> params,
            Function<URI, INITIAL_DATA> boundUriToInitialPayload,
            Function<Map<String,String>, One<CODE>> queryParamsMapper,
            BiFunction<INITIAL_DATA, CODE, One<TOKEN>> finalRequestHandler
            ) {

        Map<String, Object> paramMap = MapBuilder.of(ManualParams.class)
            .addCustomHandler("bindExplicit", (args, map) -> map.put("bind",
                args.length == 1
                ? Bind.explicit((URI) args[0])
                : Bind.explicit((URI) args[0],(KeyStore)args[1], (char[])args[2])
                ))
            .toMap(params);

        Bind bind = (Bind) paramMap.getOrDefault("bind", Bind.any());
        Duration timeout = (Duration) paramMap.getOrDefault("timeout", Duration.ofMinutes(1));
        String successHtml = (String) paramMap.getOrDefault("successHtml",
                "<html><body>Success</body></html>");
        boolean remapFragmentAsQuery = (boolean) paramMap.getOrDefault("remapFragmentAsQuery", false);

        One<HttpServer> serverRes = bindHttpServer(bind);
        if (! (serverRes instanceof Some(HttpServer server))) {
            return One.fail(serverRes.toString());
        }

        CompletableFuture<One<CODE>> initialResponseFuture = new CompletableFuture<>();

        String listenPath = bind instanceof Explicit(URI uri,_)
            ? uri.normalize().getPath() : "/";

        String queryPath = remapFragmentAsQuery
            ? listenPath + "remap-fragment"
            : listenPath;

        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();

            if (path.equals(queryPath)) {
                server.removeContext(exchange.getHttpContext());

                Map<String, String> queryParams = exchange.getRequestURI().getQuery() instanceof String query
                    ? Arrays.stream(query.split("&"))
                        .filter(s -> s.contains("=")).map(s -> s.split("="))
                        .collect(Collectors.toMap(arr -> arr[0], arr -> arr.length > 1 ? arr[1] : ""))
                    : Map.of();

                One<CODE> code = queryParamsMapper.apply(queryParams);

                String html = switch(code) {
                    case Some(_) -> successHtml;
                    case Fail<?> _ -> "<html><body>Failed</body></html>";
                };

                byte[] bytes = html.getBytes();
                int status = 200;
                exchange.sendResponseHeaders(status, bytes.length);
                try (var out = exchange.getResponseBody()) {
                    out.write(html.getBytes());
                }

                initialResponseFuture.complete(code);

            } else if (path.equals(listenPath)) {
                // write a page including javascript to parse fragment parameters,
                // and send them as query parameters instead
                byte[] html = """
                    <html>
                    <script>
                        window.location.href = "%s?" + new URLSearchParams(window.location.hash.substring(1))
                    </script>
                    </html>
                    """.formatted(queryPath).getBytes();
                exchange.sendResponseHeaders(200, html.length);
                try (var out = exchange.getResponseBody()) {
                    out.write(html);
                }
            }
        });

        URI boundAddress = inetSocketAddressToURI(
                server.getAddress(),
                listenPath,
                bind instanceof Explicit(_,Some(_)));

        server.start();

        try {
            INITIAL_DATA payload = boundUriToInitialPayload.apply(boundAddress);
            One<CODE> codeRes = initialResponseFuture.get(timeout.toSeconds(), TimeUnit.SECONDS);
            return switch(codeRes) {
                case Some(var code) -> finalRequestHandler.apply(payload, code);
                case Fail(int status, String msg) -> One.fail(status, msg);
            };
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
            return One.fail(ex.getMessage());
        } catch(ExecutionException ex) {
            return One.fail(ex.getCause().getMessage());
        } catch(TimeoutException ex) {
            return One.fail("Timeout: %ds".formatted(timeout.toSeconds()));
        } finally {
            Thread.ofPlatform().start(() -> { try { Thread.sleep(Duration.ofSeconds(3));
                server.stop(0);
            } catch (Exception _) {} });
        }
    }

    private static One<HttpServer> bindHttpServer(Bind bindMode) {
        final InetSocketAddress bindInetSocketAddress = switch(bindMode) {
            case Any() -> new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
            case Explicit(URI bind,_) -> uriToInetSocketAddress(bind);
        };
        return switch (bindMode) {
            case Explicit(_, Some(KeyStoreAndPass(KeyStore keyStore, char[] pass))) -> {
                try {
                    HttpsServer httpsServer = HttpsServer.create(bindInetSocketAddress, 0);
                    SSLContext tls = SSLContext.getInstance("TLS");
                    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    kmf.init(keyStore, pass);
                    for (int i = 0; i < pass.length; i++) pass[i] = 0;
                    tls.init(kmf.getKeyManagers(), null, null);
                    httpsServer.setHttpsConfigurator(new HttpsConfigurator(tls));
                    yield One.entry(httpsServer);
                } catch (IOException
                        | NoSuchAlgorithmException
                        | UnrecoverableKeyException
                        | KeyStoreException
                        | KeyManagementException ex) {
                    yield One.fail(ex.getMessage());
               }
            }
            default -> {
                try {
                    yield One.entry(HttpServer.create(bindInetSocketAddress, 0));
                } catch (IOException ex) {
                    yield One.fail(ex.getMessage());
                }
            }
        };
    }

    private static byte[] randomBytes(int num) {
        var bytes = new byte[num];
        new Random().nextBytes(bytes);
        return bytes;
    }

    private static String encodeBytes(byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes)
            .replaceAll(  "=",  "")
            .replaceAll("\\+", "-")
            .replaceAll("\\/", "_");
    }
}
