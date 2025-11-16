package chariot.internal.yayson;

import module java.base;

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
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < json.length(); i++) {
            switch (json.charAt(i)) {
                case '"' -> { return new JsonString(sb.toString(), json.substring(1,i)); }
                case '\\' -> {
                    i++;
                    switch(json.charAt(i)) {
                        case 'b' -> sb.append('\b');
                        case 't' -> sb.append('\t');
                        case 'n' -> sb.append('\t');
                        case 'f' -> sb.append('\f');
                        case 'r' -> sb.append('\r');
                        case 'u' -> {
                            i++;
                            String udigits = json.substring(i, i+4);
                            sb.append((char)Integer.parseInt(udigits, 16));
                            i += 3;
                        }
                        case '"',
                             '\'',
                             '\\',
                             '/' -> sb.append(json.charAt(i));
                        default -> System.err.println("Illegal escape in " + json);
                    }
                }
                default -> sb.append(json.charAt(i));
            }
        }
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
}
