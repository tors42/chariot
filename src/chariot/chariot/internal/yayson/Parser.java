package chariot.internal.yayson;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.*;

import chariot.internal.yayson.Token.*;

public class Parser {

    public static class YayException extends RuntimeException {
        public YayException(String message) {
            super(message);
        }
    }

    public sealed interface YayNode {}
    public sealed interface YayValue extends YayNode {}

    public record YayArray(List<YayNode> value)         implements YayNode {
        public <T,R> List<R> filterCastMap(Function<T, R> mapper, Class<T> cls) {
            return value.stream()
                .filter(cls::isInstance)
                .map(cls::cast)
                .map(mapper)
                .toList();
        }
    }

    public record YayObject(Map<String, YayNode> value) implements YayNode {
        public String getString(String key) {
            return value().get(key) instanceof YayString(var value) ? value : null;
        }
        public Number getNumber(String key) {
            return value().get(key) instanceof YayNumber(var num) ? num : null;
        }
        public Integer getInteger(String key) {
            return value().get(key) instanceof YayNumber(var num) ? num.intValue() : null;
        }
        public Long getLong(String key) {
            return value().get(key) instanceof YayNumber(var num) ? num.longValue() : null;
        }
        public boolean getBool(String key) {
            return value().get(key) instanceof YayBool(var bool) ? bool : false;
        }
        public List<YayNode> getArray(String key) {
            return value().get(key) instanceof YayArray(var arr) ? arr : null;
        }

        public <T,R> Map<String, R> filterCastMap(Function<T, R> mapper, Class<T> cls) {
            return cast(cls)
                .map(entry -> Map.entry(entry.getKey(), mapper.apply(entry.getValue())))
                // want a SequencedMap,
                // .collect(Collectors.toUnmodifiableMap(keyExtract, valueExtract)) gives unordered :(
                // so using reduce() with LinkedHashMap to preserve insertion order...
                .reduce(new LinkedHashMap<String, R>(), (acc, entry) -> {
                        acc.put(entry.getKey(), entry.getValue());
                        return acc;
                    },
                    (acc1, acc2) -> acc1);
        }

        private <T> Stream<Entry<String, T>> cast(Class<T> cls) {
            return value.entrySet().stream()
                .filter(entry -> cls.isInstance(entry.getValue()))
                .map(entry -> Map.entry(entry.getKey(), cls.cast(entry.getValue())));
        }
    }

    public record YayNumber(Number value) implements YayValue {}
    public record YayString(String value) implements YayValue {}
    public record YayBool(boolean value)  implements YayValue {}
    public record YayNull()               implements YayValue {}


    static YayNode parse(List<Token> tokens) {
        if (tokens.isEmpty()) {
            return null;
        }
        var token = tokens.remove(0);
        return switch(token) {
            case Structural structural -> switch(structural) {
                case BEGIN_ARRAY  -> parseArray(tokens);
                case BEGIN_OBJECT -> parseObject(tokens);
                default -> null;
            };
            case Literal literal -> switch(literal) {
                case FALSE -> new YayBool(false);
                case TRUE  -> new YayBool(true);
                case NULL  -> new YayNull();
            };
            case JsonNumber(var __, var num)    -> new YayNumber(num);
            case JsonString(String str, var __) -> new YayString(str.translateEscapes());
        };
    }

    static YayArray parseArray(List<Token> tokens) {
        var yayArray = new YayArray(new ArrayList<>());

        if (tokens.get(0) instanceof Structural s && s == Structural.END_ARRAY) {
            tokens.remove(0);
            return yayArray;
        }

        while (true) {
            var node = parse(tokens);
            yayArray.value().add(node);
            var t = tokens.remove(0);
            if (t instanceof Structural s && s == Structural.END_ARRAY) {
                return yayArray;
            } else if (! (t instanceof Structural s && s == Structural.VALUE_SEPARATOR)) {
                throw new YayException("Expected comma in array");
            }
        }
    }

    static YayObject parseObject(List<Token> tokens) {
        var yayObject = new YayObject(new LinkedHashMap<String, YayNode>());
        if (tokens.get(0) instanceof Structural eo && eo == Structural.END_OBJECT) {
            tokens.remove(0);
            return yayObject;
        }
        while (true) {
            var key = tokens.remove(0);
            if (! (key instanceof JsonString js)) {
                System.out.println("tokens: " + tokens);
                throw new YayException("Expected the key, in the JSON key-value pair, to be a string - was [%s]".formatted(key));
            }
            var sep = tokens.remove(0);
            if (! (sep instanceof Structural ns && ns == Structural.NAME_SEPARATOR)) {
                throw new YayException("Expected the colon between key-value pair - at key [%s]".formatted(key));
            }
            var node = parse(tokens);

            var prev = yayObject.value().put(js.string(), node);
            if (prev != null) {
                // warn, json "should not" have multiple keys with same name...
                System.out.println("Whaaat - prev!=null with prev " + prev);
            }
            if (tokens.isEmpty()) {
                System.out.println("\n\n\nFinished with [" + key + "] and now all tokens are gone...!?\n\n\n");
                return yayObject;
            }

            var t = tokens.remove(0);
            if (t instanceof Structural eo && eo == Structural.END_OBJECT) {
                return yayObject;
            } else if (! (t instanceof Structural vs && vs == Structural.VALUE_SEPARATOR)) {
                throw new YayException("Expected comma after pair in object - at key [%s] - the token:%n%s%n".formatted(key, t));
            }
        }
    }

    public static YayNode fromString(String json) {
        Objects.requireNonNull(json);
        json = json.trim();
        var tokens = Lexer.tokenize(json);
        var node = parse(tokens);
        //node = new YayNode.YayWithRaw(node, json);
        return node;
    }

}
