package chariot.internal;

import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.module.ModuleFinder;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import chariot.model.*;

public class Util {

    public static final String javaVersion = _javaVersion();
    public static final String clientVersion = _clientVersion();
    public static final Optional<String> mainAppVersion = _mainAppVersion();

    public static String orEmpty(String s) {
        return s == null ? "" : s;
    }

    public static ZonedDateTime fromLong(Long time) {
        if (time == null) time = 0l;
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).withNano(0);
    }

    public static String urlEncode(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    public static String urlEncode(Map<String, ?> map) {
        return map.entrySet().stream()
            .map(kv -> urlEncode(kv.getKey()) + "=" + urlEncode(stringify(kv.getValue())))
            .collect(Collectors.joining("&"));
    }

    private static String stringify(Object obj) {
        if (obj instanceof Float f) {
            // Strip trailing zeros
            // 15.0 -> 15
            // 0.75 -> 0.75
            return (f % 1.0 == 0 ? "%.0f" : "%s").formatted(f);
        } else {
            return String.valueOf(obj);
        }
    }

    public static String urlEncodeWithWorkaround(Map<String, Object> map) {
        return map.entrySet().stream()
            .map(e -> {
                // Workaround for repeating same parameters multiple times,
                // is that the mapped value is of type String[]
                if (e.getValue() instanceof String[] arr) {
                    // Input:           "status=["10", "20", "30"]
                    // Expected result: "status=10&status=20&status=30"
                    var arrayString = Arrays.stream(arr)
                        .map(val -> urlEncode(e.getKey()) + "=" + urlEncode(val))
                        .collect(Collectors.joining("&"));
                    return arrayString;
                } else {
                    return urlEncode(e.getKey()) + "=" + urlEncode(String.valueOf(e.getValue()));
                }
            })
            .collect(Collectors.joining("&"));
    }

    public static String _clientVersion() {
        return moduleNameAndVersion("chariot")
            .orElseGet(() -> {
                String version = Util.class.getPackage().getImplementationVersion();
                if (version == null) version = "?";
                return "chariot/" + version;
            });
    }

    public static String _javaVersion() {
        return "Java/" + Runtime.version().version().stream().map(String::valueOf).collect(Collectors.joining("."));
    }

    public static Optional<String> _mainAppVersion() {
        String mainModule = System.getProperty("jdk.module.main");
        if (mainModule != null) return moduleNameAndVersion(mainModule);
        return Optional.empty();
    }

    private static Optional<String> moduleNameAndVersion(String module) {
        try {
            return ModuleFinder.ofSystem().find(module)
                .or(() -> {
                    try { return ModuleFinder.of(Path.of(System.getProperty("java.class.path"))).find(module);
                    } catch (Exception __) {}
                    return Optional.empty();
                })
            .map(reference -> reference.descriptor().toNameAndVersion());
        } catch(Exception __) {}
        return Optional.empty();
    }

    public class MediaType {
        public static final String json        = "application/json";
        public static final String jsonstream  = "application/x-ndjson";
        public static final String lichessjson = "application/vnd.lichess.v3+json";
        public static final String wwwform     = "application/x-www-form-urlencoded";
        public static final String chesspgn    = "application/x-chess-pgn";
        public static final String plain       = "text/plain; charset=utf-8";
    }

    public enum Method {
        HEAD,
        GET,
        POST,
        PUT,
        DELETE,
    }

    public static class PageSpliterator<T> implements Spliterator<T> {
        Page<T> page;
        Function<Integer, Page<T>> search;

        int currentElementIndex = 0;

        public PageSpliterator(Page<T> page, Function<Integer, Page<T>> search) {
            this.page = page;
            this.search = search;
        }

        public static <T> PageSpliterator<T> of(Page<T> page, Function<Integer, Page<T>> search) {
            return new PageSpliterator<>(page, search);
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            try {
                var currentResults = page.currentPageResults();
                if (currentElementIndex < currentResults.size()) {
                    action.accept(currentResults.get(currentElementIndex));
                    currentElementIndex++;
                    return true;
                } else {
                    if (page.nbPages() instanceof Integer nbPages && page.currentPage() >= nbPages
                        || page.nextPage() == null) {
                        return false;
                    }
                    currentElementIndex = 0;
                    page = search.apply(page.nextPage());
                    return tryAdvance(action);
                }

            } catch (Exception e) {
                // ignore
            }

            return false;
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return page.nbResults() instanceof Integer nb ? nb : Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return page.nbResults() instanceof Integer
                ? (ORDERED | SIZED)
                : ORDERED;
        }
    }

    /**
     * An iterator of Pgn-modelled games.
     * It lazily reads line after line of PGN data, possibly many games,
     * and assembles these lines into Pgn models.
     */
    public static record PgnSpliterator(Iterator<String> iterator) implements Spliterator<Pgn> {
        @Override
        public boolean tryAdvance(Consumer<? super Pgn> action) {
            List<String> tagList = new ArrayList<>();
            List<String> moveList = new ArrayList<>();

            boolean comment = false;
            int consecutiveEmptyLines = 0;
            boolean tagsDone = false;
            while (iterator.hasNext() && consecutiveEmptyLines != 2) {
                String line = iterator.next();
                if (! tagsDone) {
                    if (line.startsWith("[")) {
                        tagList.add(line);
                        continue;
                    } else {
                        tagsDone = true;
                        if (line.isBlank()) continue;
                    }
                }

                moveList.add(line);

                if (line.isBlank()) {
                    if (! comment) consecutiveEmptyLines++;
                    continue;
                } else {
                    consecutiveEmptyLines = 0;
                }

                int balance = line.chars().map(c -> switch(c) {
                    case '{' -> 1;
                    case '}' -> -1;
                    default -> 0;
                }).sum();

                comment = switch(balance) {
                    case -1 -> false;
                    case 1 -> true;
                    default -> comment;
                };
            }

            if (tagList.isEmpty() && moveList.isEmpty()) return false;

            int empty = (int) moveList.reversed().stream().takeWhile(String::isBlank).count();
            if (empty > 0) moveList = moveList.subList(0, moveList.size()-empty);

            String moves = String.join("\n", moveList);
            List<Pgn.Tag> tags = tagList.stream().map(Pgn.Tag::parse).toList();

            action.accept(Pgn.of(tags, moves));

            return true;
        }

        @Override public Spliterator<Pgn> trySplit() { return null; }
        @Override public long estimateSize() { return Long.MAX_VALUE; }
        @Override public int characteristics() { return ORDERED; }
    }

    public static Map<String, String> generateUserEntryCodes(String tournamentEntryCode, Set<String> userIds) {
        var map = new HashMap<String, String>(userIds.size());
        try {
            byte[] sbytes = tournamentEntryCode.getBytes("UTF8");
            var key = new javax.crypto.spec.SecretKeySpec(sbytes, "HmacSHA256");
            var mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(key);

            for (String userId : userIds) {
                byte[] dbytes = userId.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
                mac.update(dbytes);
                byte[] bytes = mac.doFinal();
                String hex = java.util.HexFormat.of().formatHex(bytes);
                map.put(userId, hex);
            }
        } catch(UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new RuntimeException(ex);
        }
        return map;
    }

    public static Stream<Pgn> toPgnStream(Stream<String> stream) {
        return StreamSupport.stream(new PgnSpliterator(stream.iterator()), false);
    }

    public static String stripSensitive(String headerName, String value) {
        String stripped = switch(headerName.toLowerCase(Locale.ROOT)) {
            case "authorization" -> value.split(" ").length == 2 ?
                "%s ***".formatted(value.split(" ")[0]) :
                "Not on format \"<scheme> ***\"";
            default -> value;
        };
        return "%s: %s".formatted(headerName, stripped);
    }

    public static class MapBuilder<T> {
        final T forInterface;
        final Map<String, Object> map;
        final InvocationHandler basicHandler;
        final Map<String, InvocationHandler> customHandlers;

        public MapBuilder(T forInterface, Map<String,Object> map, InvocationHandler basicHandler, Map<String, InvocationHandler> customHandlers) {
            this.forInterface = forInterface;
            this.map = map;
            this.basicHandler = basicHandler;
            this.customHandlers = customHandlers;
        }

        public MapBuilder<T> addCustomHandler(String methodName, BiConsumer<Object[], Map<String,Object>> argsConsumer) {
            customHandlers.put(methodName, (proxy, method, args) -> {;
                argsConsumer.accept(args, map);
                return proxy;
            });
            return this;
        }

        public MapBuilder<T> rename(String from, String to) {
            return addCustomHandler(from, (a, m) -> m.put(to, a[0]));
        }

        public static <T> MapBuilder<T> of(Class<T> interfaceClazz) {
            final Map<String, Object> map = new LinkedHashMap<>();
            final Map<String, InvocationHandler> customHandlers = new HashMap<>();
            final InvocationHandler basicHandler = (proxy, method, args) -> {
                map.put(method.getName(), args != null ? args[0] : null);
                return proxy;
            };
            Object proxyInstance = Proxy.newProxyInstance(
                    interfaceClazz.getClassLoader(),
                    new Class<?>[] { interfaceClazz },
                    (proxy, method, args) -> {

                        if (method.isDefault()) {

                            Class<?> declaringClass = method.getDeclaringClass();
                            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup());
                            MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
                            MethodHandle handle = Modifier.isStatic(method.getModifiers()) ?
                                lookup.findStatic(declaringClass, method.getName(), methodType) :
                                lookup.findSpecial(declaringClass, method.getName(), methodType, declaringClass);

                            Object result = handle.bindTo(proxy).invokeWithArguments(args);
                            return result;
                        }

                        var handler = customHandlers.getOrDefault(method.getName(), basicHandler);
                        try {
                            var result = handler.invoke(proxy, method, args);
                            return result;
                        } catch (InvocationTargetException ex) {
                            throw ex.getCause();
                        }
                    });

            @SuppressWarnings("unchecked")
            T forInterface = (T) proxyInstance;
            return new MapBuilder<T>(forInterface, map, basicHandler, customHandlers);
        }

        public MapBuilder<T> add(String key, Object value) {
            map.put(key, value);
            return this;
        }

        public Map<String, Object> toMap(Consumer<T> consumer) {
            consumer.accept(forInterface);
            return map;
        }

        public Map<String, Object> toMap() {
            return map;
        }

        public T proxy() {
            return forInterface;
        }
    }

    public static <T> Stream<T> filterCast(Collection<? super T> coll, Class<T> cls) {
        return coll.stream().filter(cls::isInstance).map(cls::cast);
    }

}
