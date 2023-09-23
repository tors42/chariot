package chariot.internal;

import java.net.URI;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import chariot.api.Builders.*;

// servers
//  - api
//  - explorer
//  - tablebase
//  - engine
// logging
//  - request
//  - response
//  - auth
// retries
// token

public sealed interface Config {

    record Auth(Basic basic, Supplier<char[]> token)            implements Config {
        @Override public String toString() { return "Auth[%s]".formatted(basic()); }
    }
    record Basic(Servers servers, Logging logging, int retries, UAInfo uaInfo) implements Config {

        // boilerplate begin (can be replaced when reconstruction is in place - https://github.com/openjdk/amber-docs/blob/master/eg-drafts/reconstruction-records-and-classes.md)
        sealed interface Component permits Config.Servers, Config.Logging , Retries, UAInfo {}
        record Retries(int value) implements Component { Retries { if (value < 0) throw new IllegalArgumentException("Retries (" + value + ") must not be < 0"); } }
        Basic with(Component component) { return new Basic(
                component instanceof Servers   c ? c       : servers,
                component instanceof Logging   c ? c       : logging,
                component instanceof Retries   c ? c.value : retries,
                component instanceof UAInfo    c ? c       : uaInfo);
        }
        Basic with(Component... components) {
            var copy = this;
            for (var component : components) copy = copy.with(component);
            return copy;
        }
        // boilerplate end
    }

    record Servers(URI api, URI explorer, URI tablebase, URI engine) implements Basic.Component {

        public static Servers of() {
            URI api       = URI.create("https://lichess.org");
            URI explorer  = URI.create("https://explorer.lichess.ovh");
            URI tablebase = URI.create("https://tablebase.lichess.ovh");
            URI engine    = URI.create("https://engine.lichess.ovh");
            return new Servers(api, explorer, tablebase, engine);
        }

        public Servers {
            Objects.requireNonNull(api);
            Objects.requireNonNull(explorer);
            Objects.requireNonNull(tablebase);
            Objects.requireNonNull(engine);
        }

        // bp
        sealed interface Component permits Api, Explorer, Tablebase, Engine {}
        record Api(URI value)       implements Component { Api { Objects.requireNonNull(value); } }
        record Explorer(URI value)  implements Component { Explorer { Objects.requireNonNull(value); } }
        record Tablebase(URI value) implements Component { Tablebase { Objects.requireNonNull(value); } }
        record Engine(URI value)    implements Component { Engine { Objects.requireNonNull(value); } }
        Servers with(Component component) { return new Servers(
                component instanceof Api       c ? c.value : api,
                component instanceof Explorer  c ? c.value : explorer,
                component instanceof Tablebase c ? c.value : tablebase,
                component instanceof Engine    c ? c.value : engine);
        }
        Servers with(Component... components) {
            var copy = this;
            for (var component : components) copy = copy.with(component);
            return copy;
        }
        // bp
    }

    record Logging(Logger request, Logger response, Logger auth, int instanceNum) implements Basic.Component {
        private static int globalCount = 0;

        static Logging of() {
            var logging = new Logging(globalCount++);
            logging.request().setLevel(Level.WARNING);
            logging.response().setLevel(Level.WARNING);
            logging.auth().setLevel(Level.OFF);
            return logging;
        }

        public Logging {
            Objects.requireNonNull(request);
            Objects.requireNonNull(response);
            Objects.requireNonNull(auth);
        }

        Logging(int num) {
            this(Logger.getLogger("chariot.request." + num),
                 Logger.getLogger("chariot.response." + num),
                 Logger.getLogger("chariot.auth." + num),
                 num);
        }
    }

    record UAInfo(String identifier, boolean replace) implements Basic.Component { public UAInfo { Objects.requireNonNull(identifier); } }

    enum ServerType {
        api,
        explorer,
        tablebase,
        engine
    }

    static Basic basic(Consumer<ConfigBuilder> params) {
        var builder = new BasicConfigBuilder();
        params.accept(builder);
        return builder.basic;
    }

    static Basic of() { return basic(__ -> {}); }

    default Basic basic() {
        return this instanceof Basic basic ? basic : ((Auth) this).basic();
    }

    default int     retries()   { return basic().retries();   }
    default Servers servers()   { return basic().servers();   }
    default Logging logging()   { return basic().logging();   }
    default String  userAgent() {
        String identifier = basic().uaInfo().identifier();
        if (basic().uaInfo().replace()) return identifier;
        if (identifier.isEmpty()) return defaultUserAgent();
        return "%s %s".formatted(defaultUserAgent(), identifier);
    }

    default void store(Preferences prefs) {
        try{prefs.clear();}catch(BackingStoreException bse){}

        prefs.put("api", servers().api().toString());
        prefs.put("explorer", servers().explorer().toString());
        prefs.put("tablebase", servers().tablebase().toString());
        prefs.put("engine", servers().engine().toString());
        prefs.put("chariot.request", logging().request().getLevel().toString());
        prefs.put("chariot.response", logging().response().getLevel().toString());
        prefs.put("chariot.auth", logging().auth().getLevel().toString());
        prefs.put("retries", String.valueOf(retries()));
        prefs.put("ua-identifier", basic().uaInfo().identifier());
        prefs.putBoolean("ua-replace", basic().uaInfo().replace());
        if (this instanceof Auth auth) prefs.put("auth", String.valueOf(auth.token().get()));

        try{prefs.flush();}catch(BackingStoreException bse){}
    }

    static Config load(Preferences prefs) {
        var builder = new BasicConfigBuilder();
        lookup("api",              prefs, value -> builder.servers(s -> s.api(value)));
        lookup("explorer",         prefs, value -> builder.servers(s -> s.explorer(value)));
        lookup("tablebase",        prefs, value -> builder.servers(s -> s.tablebase(value)));
        lookup("engine",           prefs, value -> builder.servers(s -> s.engine(value)));
        lookup("chariot.request",  prefs, value -> builder.logging(l -> l.request().parse(value)));
        lookup("chariot.response", prefs, value -> builder.logging(l -> l.response().parse(value)));
        lookup("chariot.auth",     prefs, value -> builder.logging(l -> l.auth().parse(value)));
        lookup("retries",          prefs, value -> builder.retries(Integer.parseInt(value)));

        String uaIdentifier = prefs.get("ua-identifier", "");
        boolean uaReplace = prefs.getBoolean("ua-replace", Boolean.FALSE);
        builder.userAgent(uaIdentifier, uaReplace);

        Config config = builder.basic;
        String value = prefs.get("auth", prefs.get("omni_", null));
        return value == null ? config : config.withToken(value);
    }

    private static void lookup(String key, Preferences prefs, Consumer<String> consumer) {
        String value = prefs.get(key, null);
        if (value != null) consumer.accept(value);
    }

    static void clearAuth(Preferences prefs) {
        prefs.remove("auth");
        try {
            Arrays.stream(prefs.keys())
                .filter(key -> List.of("omni_", "pre_").stream()
                        .anyMatch(prefix -> key.startsWith(prefix)))
                .forEach(key -> prefs.remove(key)); }
        catch (Exception e) {}
        try { prefs.flush();} catch (Exception e) {}
    }

    static String defaultUserAgent() {
        return "%s %s".formatted(Util.javaVersion, Util.clientVersion) + Util.mainAppVersion.map(value -> " " + value).orElse("");
    }

    class BasicConfigBuilder implements ConfigBuilder {
        Basic basic = new Basic(Servers.of(), Logging.of(), 1 /*retries*/, new UAInfo("", false));

        @Override
        public ConfigBuilder api(URI uri) {
            basic = basic.with(basic.servers().with(new Config.Servers.Api(uri)));
            return this;
        }

        @Override
        public ConfigBuilder servers(Consumer<ServerBuilder> params) {
            var serverBuilder = new DefaultServerBuilder(basic.servers());
            params.accept(serverBuilder);
            basic = basic.with(serverBuilder.servers);
            return this;
        }

        @Override
        public ConfigBuilder logging(Consumer<LoggingBuilder> params) {
            var loggingBuilder = loggingBuilder(basic.logging());
            params.accept(loggingBuilder);
            // no need to update basic, because logging changes levels of the referenced loggers, it doesn't create new loggers...
            return this;
        }

        @Override
        public ConfigBuilder retries(int retries) {
            basic = basic.with(new Config.Basic.Retries(retries));
            return this;
        }

        @Override
        public ConfigBuilder userAgent(String identifier, boolean replaceAll) {
            basic = basic.with(new Config.Basic.UAInfo(identifier, replaceAll));
            return this;
        }
    }

    class DefaultServerBuilder implements ServerBuilder {
        Servers servers;
        DefaultServerBuilder(Servers initial) { servers = initial; }
        @Override public ServerBuilder api(URI uri)       { return component(Servers.Api::new, uri); }
        @Override public ServerBuilder explorer(URI uri)  { return component(Servers.Explorer::new, uri); }
        @Override public ServerBuilder tablebase(URI uri) { return component(Servers.Tablebase::new, uri); }
        @Override public ServerBuilder engine(URI uri)    { return component(Servers.Engine::new, uri); }

        private ServerBuilder component(Function<URI, Servers.Component> constr, URI uri) {
            servers = servers.with(constr.apply(uri));
            return this;
        }
    }

    class DefaultLoggingBuilder implements LoggingBuilder {
        Logging logging;
        DefaultLoggingBuilder(Logging initial) { logging  = initial; }
        @Override public LogLevel request()  { return logLevel(logging.request()); }
        @Override public LogLevel response() { return logLevel(logging.response()); }
        @Override public LogLevel auth()     { return logLevel(logging.auth()); }

        private LogLevel logLevel(Logger logger) {
            return new LogLevel() {
                @Override public LoggingBuilder all()     { return level(Level.ALL); }
                @Override public LoggingBuilder finest()  { return level(Level.FINEST); }
                @Override public LoggingBuilder finer()   { return level(Level.FINER); }
                @Override public LoggingBuilder fine()    { return level(Level.FINE); }
                @Override public LoggingBuilder config()  { return level(Level.CONFIG); }
                @Override public LoggingBuilder info()    { return level(Level.INFO); }
                @Override public LoggingBuilder warning() { return level(Level.WARNING); }
                @Override public LoggingBuilder severe()  { return level(Level.SEVERE); }
                @Override public LoggingBuilder off()     { return level(Level.OFF); }
                @Override public LoggingBuilder parse(String level) { return level(Level.parse(level)); }

                private LoggingBuilder level(Level level) {
                    logger.setLevel(level);
                    return DefaultLoggingBuilder.this;
                }
            };
        }
    }

    public static LoggingBuilder loggingBuilder(Logging initial) { return new DefaultLoggingBuilder(initial); }




    static Auth auth(Consumer<ConfigBuilder> params, String token) {
        return basic(params).withToken(token);
    }

    default Auth withToken(Supplier<char[]> token) {
        var enc = Crypt.encrypt(token.get());
        return new Auth(basic(), () -> Crypt.decrypt(enc.data(), enc.key()));
    }

    default Auth withToken(String token) {
        return withToken(token::toCharArray);
    }

}
