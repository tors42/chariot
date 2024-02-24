package chariot.internal.yayson;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.List;

public sealed interface Token {

    static final Token BEGIN_OBJECT = Structural.BEGIN_OBJECT;
    static final Token END_OBJECT = Structural.END_OBJECT;
    static final Token BEGIN_ARRAY = Structural.BEGIN_ARRAY;
    static final Token END_ARRAY = Structural.END_ARRAY;
    static final Token NAME_SEPARATOR = Structural.NAME_SEPARATOR;
    static final Token VALUE_SEPARATOR = Structural.VALUE_SEPARATOR;
    static final Token NULL = Literal.NULL;
    static final Token TRUE = Literal.TRUE;
    static final Token FALSE = Literal.FALSE;

    int length();

    static enum Structural implements Token {
        BEGIN_OBJECT('{'),
        END_OBJECT('}'),
        BEGIN_ARRAY('['),
        END_ARRAY(']'),
        NAME_SEPARATOR(':'),
        VALUE_SEPARATOR(','),
        ;
        final char token;
        Structural(char token) { this.token = token; }
        static Structural parse(char token) {
            return switch(token) {
                case '{' -> BEGIN_OBJECT;
                case '}' -> END_OBJECT;
                case '[' -> BEGIN_ARRAY;
                case ']' -> END_ARRAY;
                case ':' -> NAME_SEPARATOR;
                case ',' -> VALUE_SEPARATOR;
                default -> null;
            };
        }
        @Override public int length() { return 1; }
        public char token() { return token; }
    }

    static enum Literal implements Token {
        TRUE("true"),
        FALSE("false"),
        NULL("null"),
        ;
        final String literal;
        final int length;
        Literal(String literal) {
            this.literal = literal;
            length = literal.length();
        }
        static Literal parse(String str) {
            if (str.startsWith("true")) return TRUE;
            if (str.startsWith("null")) return NULL;
            if (str.startsWith("false")) return FALSE;
            return null;
        }
        @Override public int length() { return length; }
        public String literal() { return literal; }
    }

    record JsonNumber(String string, Number number) implements Token {
        @Override public int length() { return string().length(); }
    };

    record JsonString(String string, String source) implements Token {
        public JsonString(String string) { this(string, string); }

        public static JsonString decode(String json) {
            return switch(json) {
                case String str when str.contains("\\") ->
                    new JsonString(
                        str.replaceAll("\\\\/", "/")
                            .transform(Token::decodeUnicode)
                            .translateEscapes(),
                        json);
                default -> new JsonString(json);
            };
        }
        @Override public int length() { return source.length() + 2; } // +2 for quotation marks
    };


    record TokenAndTail(Optional<Token> token, String tail) {}

    static TokenAndTail lex(String json) {
        return switch(parseToken(json)) {
            case null        -> new TokenAndTail(Optional.empty(),   json.isEmpty() ? "" : json.substring(1).trim());
            case Token token -> new TokenAndTail(Optional.of(token), json.length() > token.length() ? json.substring(token.length()).trim() : "");
        };
    }

    private static Token parseToken(String str) {
        return switch(Character.valueOf(str.charAt(0))) {
            case Character c when Structural.parse(c) instanceof Structural structural
                -> structural;
            case Character c when (Character.isDigit(c) || Character.valueOf(c) == '-') && lexNumber(str) instanceof JsonNumber number
                -> number;
            case Character c when Set.of('t', 'f', 'n').contains(c) && Literal.parse(str) instanceof Literal literal
                -> literal;
            case Character c when c.charValue() == '"' && lexString(str) instanceof JsonString string
                -> string;
            default -> null;
        };
    }

    private static Token lexString(String json) {
        int endQuote = 1;

        try {
            while((endQuote = json.indexOf('"', endQuote)) != -1) {
                // Check if the " is escaped...
                if (json.charAt(endQuote-1) == '\\') {
                    // Check if the \ is not escaped...
                    if (json.charAt(endQuote-2) != '\\') {

                        // Ok escape was real...
                        // Search for next " instead.

                        endQuote++;
                        continue;
                    }
                }

                // Ok it seems the found " is not escaped,
                // let's use everything we found.

                var parsedString = json.substring(1, endQuote);
                var string = JsonString.decode(parsedString);
                return string;
            }
        } catch (Exception e) {
            // todo error.log
            e.printStackTrace(System.err);
        }

        // todo error.log
        System.err.println("Couldn't find matching [\"], for json [" + json + "]");
        return null;
    }

    private static Token lexNumber(String json) {
        try {
            var chars = List.<Character>of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '+', 'e', 'E', '.');
            int idx = 0;
            while (idx < json.length()-1 && chars.contains(json.charAt(idx))) {
                idx++;
            }
            var string = json.substring(0, idx);

            Number number;
            if (string.contains(".")) {
               number = Float.valueOf(string);
            } else {
                var n = Long.valueOf(string);
                if (n >= 0) {
                    if (n <= Integer.MAX_VALUE) {
                        number = (Integer) n.intValue();
                    } else {
                        number = n;
                    }
                } else {
                    if (n < Integer.MIN_VALUE) {
                        number = n;
                    } else {
                        number = (Integer) n.intValue();
                    }
                }
            }
            return new JsonNumber(string, number);
        } catch (Exception e) {
            // todo error.log
            e.printStackTrace(System.err);
        }
        return null;
    }

    static String decodeUnicode(String string) {
        try {
            return _decodeUnicode(string);
        } catch (Exception e) {
            // todo, logger.errors
            e.printStackTrace(System.err);
            return string;
        }
    }

    static String _decodeUnicode(String string) throws Exception {
        byte[] bytes = string.getBytes();
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        for(int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];

            // An escape
            if (b == 0x5c) {
                if (i+1 < bytes.length) {
                    byte next = bytes[i+1];
                    // u
                    if (next == 0x75) {

                        byte ub1 = bytes[i+2];   // 50
                        byte ub2 = bytes[i+3];
                        byte ub3 = bytes[i+4];
                        byte ub4 = bytes[i+5];

                        char uc1 = (char) ub1;   // '2'
                        char uc2 = (char) ub2;
                        char uc3 = (char) ub3;
                        char uc4 = (char) ub4;

                        String four = ""+uc1+uc2+uc3+uc4;  // "2658"

                        int cp = Integer.parseInt(four, 16); // 9816

                        char[] chars = Character.toChars(cp);  // char[1] {horse}



                        // Hmm, "ud83d udcdc" -> "{scroll}"
                        //
                        // "{scroll}".getBytes()  ->  { -16, -97, -109, -100 }

                        // So after the first unicodes, check if there is a follow up,
                        // and figure out how to parse it...

                        String s = new String(chars);

                        //System.out.println("Decoded string: [" + s + "]");
                        var sbytes = s.getBytes();
                        for (int j = 0; j < sbytes.length; j++) {
                            bb.put(sbytes[j]);
                        }

                        i += 5;
                        continue;
                    }
                }
            }

            bb.put(b);
        }

        int pos = bb.position();
        return new String(bb.slice(0, pos).array());
    }

}
