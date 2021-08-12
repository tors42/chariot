package chariot.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import chariot.Client.Scope;
import chariot.api.Builders.*;

public sealed interface Config {

    final static String lichess   = "https://lichess.org";
    final static String explorer  = "https://explorer.lichess.ovh";
    final static String tablebase = "https://tablebase.lichess.ovh";
    final static String local     = "http://localhost:9663";

    record Servers(Server api, Server explorer, Server tablebase) {}
    record Logging(Logger request, Logger responsebodyraw, Logger auth) {}

    /**
     * @param url Custom API server
     */
    static Config.Basic api(String url){
        return basic(c -> c.api(url));
    }

    /**
     * Create a {@code Config.Basic}
     * @param params Configuration parameters
     */
    static Config.Basic basic(Consumer<Builder> params){
        var cbuilder = new BuilderImpl();
        params.accept(cbuilder);
        return cbuilder.build();
    }

    /**
     * Create a {@code Config.Auth}
     * @param params Configuration parameters
     */
    static Config.Auth auth(Consumer<TokenBuilder> params){
        var cbuilder = new TokenBuilderImpl();
        params.accept(cbuilder);
        Auth auth = cbuilder.buildAuth();
        if (auth == null) {
            throw new IllegalArgumentException("Missing token");
        }
        return auth;
    }

    sealed interface Server {
        record None() implements Server {}
        record Url(String url) implements Server {
            public Url {
                Objects.requireNonNull(url);
                java.net.URI.create(url);
            }
        }

        static Server of(String url) { return new Url(url); }
        static Server none() { return new None(); }

        default void ifPresent(Consumer<String> s) {
            if (this instanceof Url u) {
                s.accept(u.url());
            }
        }

        default String get() {
            if (this instanceof Url u) return u.url();
            else throw new NoSuchElementException();
        }
    }

    enum ServerType {
        api,
        explorer,
        tablebase
    }

    boolean isAuth();
    void store(Preferences prefs);

    public Servers servers();
    public Logging logging();

    static Config load(Preferences prefs) {
        var basic = loadBasic(prefs);
        return basic.withAuth(prefs).orElse(basic);
    }

    static Config.Auth load(Preferences prefs, Consumer<AuthBuilder> token) {
        var tbuilder = new TokenBuilderImpl();
        token.accept(tbuilder);
        Auth auth = tbuilder.buildAuth(loadBasic(prefs));

        return auth;
    }

    private static Basic loadBasic(Preferences prefs) {
        Objects.requireNonNull(prefs);
        return basic(c -> c
                .api(prefs.get("api", lichess))
                .servers(s -> s
                    .explorer(prefs.get("explorer", explorer))
                    .tablebase(prefs.get("tablebase", tablebase)))
                .levels(l -> l.request().warning()
                    .response().warning()
                    .auth().off()
                    ));
    }


    record Basic(Servers servers, Logging logging) implements Config {

        @Override public boolean isAuth() { return false; }

        /**
         * Enables use of authenticated endpoints, by using the supplied
         * token for authentication.
         */
        public Auth withAuth(Supplier<char[]> token) {
            return new Auth(this, new TokenType.OmniToken(token));
        }

        /**
         * Enables use of authenticated endpoints, by using the supplied
         * tokens for authentication.
         * The server will be queried for each token to see which scopes they
         * are valid for, and when making a request to an endpoint which needs
         * a specific scope, a token providing that scope will be used.
         */
        public Auth withAuth(Set<Supplier<char[]>> tokens) {
            return new Auth(this, new TokenType.AutoScopedTokens(
                        logging().auth(),
                        Collections.unmodifiableSet(tokens),
                        new HashMap<>())
                    );
        }

        /**
         * Enables use of authenticated endpoints, by using the supplied
         * tokens for authentication.
         * When making a request to an endpoint which needs a specific scope,
         * the token providing that scope will be used.
         */
         public Auth withAuth(Map<Scope, Supplier<char[]>> tokens) {
             return new Auth(this, new TokenType.PreScopedTokens(
                         Collections.unmodifiableMap(tokens))
                     );
         }


        /**
         *  Enables use of authenticated endpoints,
         *  by using the supplied token for authentication.
         */
         public Auth withAuth(String token) {
             return withAuth(token.toCharArray());
         }

        /**
         *  Enables use of authenticated endpoints,
         *  by using the supplied token for authentication.
         */
         public Auth withAuth(char[] token) {
             // User is supplying a plain text token.
             // Let's make an effort, albeit small, and obfuscate the token
             // so we don't keep it in memory in plain text.
             var enc = Crypt.encrypt(token);
             return new Auth(this, new TokenType.OmniToken(
                         () -> Crypt.decrypt(enc.data(), enc.key()))
                     );
         }

         @Override
         public void store(Preferences prefs) {
             prefs.put("api", servers().api().get());
             if (servers().explorer() instanceof Server.Url u) prefs.put("explorer", u.url); else prefs.remove("explorer");
             if (servers().tablebase() instanceof Server.Url u) prefs.put("tablebase", u.url); else prefs.remove("tablebase");
             prefs.put("chariot.request", logging().request().getLevel().toString());
             prefs.put("chariot.response-body-raw", logging().responsebodyraw().getLevel().toString());
             prefs.put("chariot.auth", logging().auth().getLevel().toString());
         }


         Optional<Config> withAuth(Preferences prefs) {

             Optional<Config> omniAuth =
                 pref(prefs, "omni_").map( omni -> {
                     var enc = Crypt.encrypt(omni.toCharArray());
                     return withAuth(() -> Crypt.decrypt(enc.data(), enc.key()));
                 });

             if (omniAuth.isPresent())
                 return omniAuth;

             Optional<Config> preAuth =
                 pref(prefs, "pre_").map( pre -> {
                     var map = new HashMap<Scope, Supplier<char[]>>();
                     var tokens = pre.split(",");
                     for (int t_idx = 0; t_idx < tokens.length; t_idx++) {
                         var enc = Crypt.encrypt(tokens[t_idx].toCharArray());
                         Supplier<char[]> token = () -> Crypt.decrypt(enc.data(), enc.key());
                         var scopes = prefs.get("pre_" + (t_idx+1), null).split(",");
                         for (int s_idx = 0; s_idx < scopes.length; s_idx++) {
                             map.put(Scope.fromString(scopes[s_idx]).get(), token);
                         }
                     }
                     return withAuth(map);
                 });

             return preAuth;
         }

         private static Optional<String> pref(Preferences prefs, String key) {
             return Optional.ofNullable(prefs.get(key, null));
         }

    }

    record Auth(Basic basic, TokenType type) implements Config {

        @Override
        public boolean isAuth() { return true; }

        @Override
        public Logging logging() {
            return basic().logging();
        }

        @Override
        public Servers servers() {
            return basic().servers();
        }

        @Override
        public void store(Preferences prefs) {
            basic.store(prefs);
            if (type instanceof TokenType.OmniToken omni) {
                prefs.put("omni_", String.valueOf(omni.token().get()));
                return;
            }
            Optional<Map<Scope, Supplier<char[]>>> map = Optional.empty();
            if (type instanceof TokenType.PreScopedTokens pre) {
                map = Optional.of(pre.tokens());
            } else if (type instanceof TokenType.AutoScopedTokens auto) {
                var less = auto.cache().entrySet().stream()
                    .collect(Collectors.toMap(
                                e -> e.getKey(),
                                e -> e.getValue().stream().findAny()
                                )
                            );
                map = Optional.of(less.entrySet().stream()
                    .filter(e -> e.getValue().isPresent())
                    .collect(Collectors.toMap(
                                e -> e.getKey(),
                                e -> e.getValue().get()
                                )
                            ));
                // Phew... The AutoScopedTokens fitting was... _is_... ugly...
                // Should be unsupported instead? Hmm...
            }

            map.ifPresent( m -> {
                var resolved = m.entrySet().stream()
                    .collect(Collectors.toMap(
                                e -> e.getKey(),
                                e -> String.valueOf(e.getValue().get())
                                )
                            );
                var tokens = resolved.values().stream()
                    .toList();

                prefs.put("pre_", String.join(",", tokens));
                for (int i = 0; i < tokens.size(); i++) {
                    var token = tokens.get(i);
                    var scopes = resolved.entrySet().stream()
                        .filter(e -> token.equals(e.getValue()))
                        .map(e -> e.getKey().asString())
                        .collect(Collectors.joining(","));
                    prefs.put("pre_"+(i+1), scopes);
                }
            });
        }
    }

    sealed interface TokenType {

        /**
         * Look up a Token to use for a given scope
         * @param scope For which scope a token is needed
         */
        public Optional<Supplier<char[]>> getToken(Scope scope);

        /**
         * Specify a single token, which will be used for all requests
         * where a scope is needed.
         */
        record OmniToken(Supplier<char[]> token) implements TokenType {
            @Override
            public Optional<Supplier<char[]>> getToken(Scope scope) {
                return Optional.ofNullable(token());
            }
        }

        /**
         *  Specify a set of tokens. Their scopes will be queried from
         *  the server.
         *  When a request needs a certain scope, a token which is valid for
         *  that scope will be used.
         */
        record AutoScopedTokens(
                Logger authlog,
                Set<Supplier<char[]>> tokens,
                Map<Scope, Set<Supplier<char[]>>> cache) implements TokenType {
            @Override
            public Optional<Supplier<char[]>> getToken(Scope scope) {
                return cache.computeIfAbsent(scope,(s) -> new HashSet<>())
                    .stream()
                    .findAny();
            }

            public void resolve(Function<Supplier<char[]>, Set<Scope>> mapper) {
                tokens().forEach(token ->
                        mapper.apply(token).forEach(scope -> {
                            authlog().info(() -> "AutoScope [%s] [%s]"
                                    .formatted(scope, String.valueOf(token.get())));
                            cache().computeIfAbsent(scope, (s) -> new HashSet<>())
                                .add(token);
                        }));

                authlog().info(cache().entrySet().stream()
                        .map(e -> "[%s] [%s]".formatted(
                                e.getKey(),
                                e.getValue().stream()
                                .map(sup -> "%s".formatted(String.valueOf(sup.get())))
                                .collect(Collectors.joining(","))
                                ))
                        .collect(Collectors.joining("\n"))
                        );
            }
        }

        /**
         *  Specify an explicit scope-to-token mapping,
         *  to control which token is used for which scope.
         */
        record PreScopedTokens(Map<Scope, Supplier<char[]>> tokens) implements TokenType {
            @Override
            public Optional<Supplier<char[]>> getToken(Scope scope) {
                return Optional.ofNullable(tokens().get(scope));
            }
        }
    }


    class BuilderImpl implements Builder {
        SBuilderImpl sbuilder = new SBuilderImpl();
        LBuilderImpl lbuilder = new LBuilderImpl();
        Server api = Server.of(lichess);

        @Override public Builder api(String api) { this.api = Server.of(api); return this; }
        @Override public Builder servers(Consumer<ExtServBuilder> params) { params.accept(sbuilder); return this; }
        @Override public Builder levels(Consumer<LogSetter> params) { params.accept(lbuilder); return this; }

        Config.Basic build() {
            return new Config.Basic(sbuilder.build(api), lbuilder.build());
        }
    }

    class SBuilderImpl implements ExtServBuilder {
        Map<String, Server> map = new HashMap<>();
        @Override public ExtServBuilder explorer(String url) { map.put("explorer", Server.of(url)); return this; }
        @Override public ExtServBuilder tablebase(String url) { map.put("tablebase", Server.of(url)); return this; }
        Servers build(Server api) { Servers server = new Servers(api, map.get("explorer"), map.get("tablebase")); return server; }
    }

    class LBuilderImpl implements LogSetter {
        Logger request         = Logger.getLogger("chariot.request");
        Logger responsebodyraw = Logger.getLogger("chariot.response-body-raw");
        Logger auth            = Logger.getLogger("chariot.auth");
        LBuilderImpl() { this(true); }
        public LBuilderImpl(boolean updateAll) { if (updateAll) { request.setLevel(Level.WARNING); responsebodyraw.setLevel(Level.WARNING); auth.setLevel(Level.OFF); } }

        Logging build() { return new Logging(request, responsebodyraw, auth); }

        @Override
        public LogLevel request() {
            return new LogLevelImpl(this, request);
        }
        @Override
        public LogLevel response() {
            return new LogLevelImpl(this, responsebodyraw);
        }
        @Override
        public LogLevel auth() {
            return new LogLevelImpl(this, auth);
        }
    }


    class LogLevelImpl implements LogLevel {
        private final Logger logger;
        private final LogSetter setter;

        private LogLevelImpl(LogSetter setter, Logger logger) {
            this.setter = setter;
            this.logger = logger;
        }

        @Override
        public LogSetter all() {
            logger.setLevel(Level.ALL);
            return setter;
        }
        @Override
        public LogSetter finest() {
            logger.setLevel(Level.FINEST);
            return setter;
        }
        @Override
        public LogSetter finer() {
            logger.setLevel(Level.FINER);
            return setter;
        }
        @Override
        public LogSetter fine() {
            logger.setLevel(Level.FINE);
            return setter;
        }
        @Override
        public LogSetter config() {
            logger.setLevel(Level.CONFIG);
            return setter;
        }
        @Override
        public LogSetter info() {
            logger.setLevel(Level.INFO);
            return setter;
        }
        @Override
        public LogSetter warning() {
            logger.setLevel(Level.WARNING);
            return setter;
        }
        @Override
        public LogSetter severe() {
            logger.setLevel(Level.SEVERE);
            return setter;
        }
        @Override
        public LogSetter off() {
            logger.setLevel(Level.OFF);
            return setter;
        }
    }

    class TokenBuilderImpl extends BuilderImpl implements TokenBuilder {
        Supplier<char[]>             token = null;
        Set<Supplier<char[]>>        tokens = null;
        Map<Scope, Supplier<char[]>> mappedTokens = null;

        @Override public TokenBuilder api(String api) { super.api(api); return this; }
        @Override public TokenBuilder servers(Consumer<ExtServBuilder> params) { super.servers(params); return this; }
        @Override public TokenBuilder levels(Consumer<LogSetter> params) { super.levels(params); return this; }

        @Override public TokenBuilder auth(Supplier<char[]> token) { this.token = token; return this; }
        @Override public TokenBuilder auth(Set<Supplier<char[]>> tokens) { this.tokens = tokens; return this; }
        @Override public TokenBuilder auth(Map<Scope, Supplier<char[]>> tokens) { this.mappedTokens = tokens; return this; }
        Config.Auth buildAuth() {
            Config.Basic basic = super.build();
            return buildAuth(basic);
        }
        Config.Auth buildAuth(Basic basic) {
            if(token != null) {
                return basic.withAuth(token);
            } else if (tokens != null) {
                return basic.withAuth(tokens);
            } else if (mappedTokens != null) {
                return basic.withAuth(mappedTokens);
            } else {
                return null;
            }
        }
    }

}
