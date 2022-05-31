package chariot.internal;

import static java.util.function.Predicate.not;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import chariot.model.*;

public class Util {

    public static final String javaVersion = _javaVersion();
    public static final String clientVersion = _clientVersion();
    public static final Predicate<String> notNullNotEmpty = not(String::isEmpty).and(Objects::nonNull);

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
                // Workaround for repeating same parameters multiple times,
                // is that the mapped value is of type String[]
                if (e.getValue() instanceof String[] arr) {
                    // Input:           "status=["10", "20", "30"]
                    // Expected result: "status=10&status=20&status=30"
                    var arrayString = Arrays.stream(arr)
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
                    if (page.currentPage() >= page.nbPages()) {
                        return false;
                    }
                    currentElementIndex = 0;
                    page = search.apply(page.nextPage());
                    return tryAdvance(action);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return page.nbResults();
        }

        @Override
        public int characteristics() {
            return ORDERED | SIZED;
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
            List<String> tagList = readGroup(iterator);
            List<String> moveList = readGroup(iterator);
            if (tagList.isEmpty() && moveList.isEmpty()) return false;

            var moves = String.join(" ", moveList);
            var tags = tagList.stream().map(Pgn.Tag::parse).toList();
            action.accept(Pgn.of(tags, moves));
            return true;
        }

        List<String> readGroup(Iterator<String> iterator) {
            var list = new ArrayList<String>();
            while (iterator.hasNext()) {
                String line = iterator.next();
                if (! line.isBlank()) {
                    list.add(line);
                    continue;
                }
                if (! list.isEmpty()) break;
            }
            return list;
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
                byte[] dbytes = userId.toLowerCase().getBytes("UTF8");
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

    public static Result<Pgn> toPgnResult(Result<String> result) {
        if (result instanceof Result.Many<String> many) {
            return Result.many(StreamSupport.stream(new PgnSpliterator(many.stream().iterator()), false));
        } else if (result instanceof Result.Fail<String> fail) {
            return Result.fail(fail.message());
        } else {
            return Result.many(Stream.of());
        }
    }
}
