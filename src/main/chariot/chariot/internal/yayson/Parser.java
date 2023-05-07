package chariot.internal.yayson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import chariot.internal.yayson.Token.*;

public class Parser {

    public static class YayException extends RuntimeException {
        public YayException(String message) {
            super(message);
        }
    }

    public sealed interface YayNode {}
    public sealed interface YayValue extends YayNode {}

    public record YayWithRaw(YayNode node, String raw)  implements YayNode {}
    public record YayEmpty()                            implements YayNode {}
    public record YayArray(List<YayNode> value)         implements YayNode {}
    public record YayObject(Map<String, YayNode> value) implements YayNode {
        public String getString(String key) {
            if (value().get(key) instanceof YayString s) {
                return s.value();
            }
            return null;
        }

        public Number getNumber(String key) {
            if (value().get(key) instanceof YayNumber n) {
                return n.value();
            }
            return null;
        }
        public Integer getInteger(String key) {
            if (value().get(key) instanceof YayNumber n) {
                return n.value().intValue();
            }
            return null;
        }
        public Long getLong(String key) {
            if (value().get(key) instanceof YayNumber n) {
                return n.value().longValue();
            }
            return null;
        }

        public boolean getBool(String key) {
            if (value().get(key) instanceof YayBool b) {
                return b.value();
            }
            return false;
        }
    }

    public record YayNumber(Number value) implements YayValue {}
    public record YayString(String value) implements YayValue {}
    public record YayBool(boolean value)  implements YayValue {}
    public record YayNull()               implements YayValue {}


    static YayNode parse(List<Token> tokens) {
        if (tokens.isEmpty()) {
            return new YayEmpty();
        }
        var token = tokens.remove(0);

        // --enable-preview
        //return switch(token) {
        //    case BeginArray  __  -> parseArray(tokens);
        //    case BeginObject __  -> parseObject(tokens);
        //    case False       __  -> new YayBool(false);
        //    case True        __  -> new YayBool(true);
        //    case Null        __  -> new YayNull();
        //    case JsonNumber  num -> new YayNumber(num.number());
        //    case JsonString  str -> new YayString(str.string());
        //};

        if (token instanceof BeginArray) {
            return parseArray(tokens);
        } else if (token instanceof BeginObject) {
            return parseObject(tokens);
        } else {
            if (token instanceof False) {
                return new YayBool(false);
            } else if (token instanceof True) {
                return new YayBool(true);
            } else if (token instanceof Null) {
                return new YayNull();
            } else if (token instanceof JsonNumber n) {
                return new YayNumber(n.number());
            } else if (token instanceof JsonString s) {
                return new YayString(s.string());
            }
            return new YayEmpty();
        }
    }

    static YayArray parseArray(List<Token> tokens) {
        var yayArray = new YayArray(new ArrayList<>());

        if (tokens.get(0) instanceof EndArray) {
            tokens.remove(0);
            return yayArray;
        }

        while (true) {
            var node = parse(tokens);
            yayArray.value().add(node);
            var t = tokens.remove(0);
            if (t instanceof EndArray) {
                return yayArray;
            } else if (! (t instanceof ValueSeparator)) {
                throw new YayException("Expected comma in array");
            }
        }
    }

    static YayObject parseObject(List<Token> tokens) {
        var yayObject = new YayObject(new HashMap<String, YayNode>());
        if (tokens.get(0) instanceof EndObject) {
            tokens.remove(0);
            return yayObject;
        }
        while (true) {
            var key = tokens.remove(0);
            if (! (key instanceof JsonString js)) {
                throw new YayException("Expected the key, in the JSON key-value pair, to be a string - was [%s]".formatted(key));
            }
            var sep = tokens.remove(0);
            if (! (sep instanceof NameSeparator)) {
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
            if (t instanceof EndObject) {
                return yayObject;
            } else if (! (t instanceof ValueSeparator)) {
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
