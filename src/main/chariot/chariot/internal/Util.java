package chariot.internal;

import static java.util.function.Predicate.not;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Util {

    public static final String javaVersion = _javaVersion();
    public static final String clientVersion = _clientVersion();
    public static final Predicate<String> notNullNotEmpty = not(String::isEmpty).and(Objects::nonNull);

    public static final ThreadFactory tf = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            var thread = Executors.defaultThreadFactory().newThread(runnable);
            // Help users who forget to shutdown client
            thread.setDaemon(true);
            return thread;
        }
    };

    public static String orEmpty(String s) {
        return s == null ? "" : s;
    }

    public static ZonedDateTime fromLong(Long time) {
        if (time == null) time = 0l;
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    }

    public static String urlEncode(Map<String, ?> map) {
        return map.entrySet().stream()
            .map(kv -> kv.getKey() + "=" + URLEncoder.encode(String.valueOf(kv.getValue()), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));
    }

    public static String urlEncodeWithWorkaround(Map<String, Object> map) {
        return map.entrySet().stream()
            .map(e -> {
                    // Workaround: if-branch (normally the else-branch is all we need...) See GamesImpl class
                    // Handle multikey/value parameters... Currently "encoded" like this:
                    // Input:           "speeds[]=a,b,c
                    // Expected result: "speeds[]=a&speeds[]=b&speeds[]=c"
                    if (e.getKey().endsWith("[]")) {
                        var vals = String.valueOf(e.getValue()).split(",");
                        var arrayString = Arrays.stream(vals)
                            .map(val -> e.getKey() + "=" + val)
                            .collect(Collectors.joining("&"));
                        return arrayString;
                    } else {
                        return e.getKey() + "=" + URLEncoder.encode(String.valueOf(e.getValue()), StandardCharsets.UTF_8);
                    }
            })
            .collect(Collectors.joining("&"));
    }

    public static String _clientVersion() {
        var module = Util.class.getModule();
        var pkg = Util.class.getPackage();
        if (module.isNamed()) {
            return String.join("/", module.getName(),
                    module.getDescriptor().version().map(v -> v.toString())
                    .orElse(Optional.ofNullable(pkg.getImplementationVersion()).orElse("<unknown>")));
        } else {
            return String.join("/", "chariot",
                    Optional.ofNullable(pkg.getImplementationVersion()).orElse("<unknown>"));
        }
    }

    public static String _javaVersion() {
        return String.join("/", "Java", Runtime.version().version().stream().map(String::valueOf).collect(Collectors.joining(".")));
    }

    public class MediaType {
        public static final String json        = "application/json";
        public static final String jsonstream  = "application/x-ndjson";
        public static final String lichessjson = "application/vnd.lichess.v3+json";
        public static final String wwwform     = "application/x-www-form-urlencoded";
        public static final String chesspgn    = "application/x-chess-pgn";
        public static final String plain       = "text/plain; charset=utf-8";
    }
}

