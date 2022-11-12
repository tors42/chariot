package chariot.internal.yayson;

import java.util.Optional;
import java.nio.ByteBuffer;
import java.util.List;

public sealed interface Token {

    // Structural
    public static final Token BEGIN_OBJECT = new BeginObject('{');
    public static final Token END_OBJECT = new EndObject('}');
    public static final Token BEGIN_ARRAY = new BeginArray('[');
    public static final Token END_ARRAY = new EndArray(']');
    public static final Token NAME_SEPARATOR = new NameSeparator(':');
    public static final Token VALUE_SEPARATOR = new ValueSeparator(',');

    // Literals
    public static final Token FALSE = new False();
    public static final Token TRUE = new True();
    public static final Token NULL = new Null();


    // Structural
    record BeginArray(char c)     implements Token {};
    record BeginObject(char c)    implements Token {};
    record EndArray(char c)       implements Token {};
    record EndObject(char c)      implements Token {};
    record NameSeparator(char c)  implements Token {};
    record ValueSeparator(char c) implements Token {};

    // Literals
    record False() implements Token {
        @Override public int length() { return "false".length(); }
    };
    record True()  implements Token {
        @Override public int length() { return "true".length(); }
    };
    record Null()  implements Token {
        @Override public int length() { return "null".length(); }
    };

    // Number and String

    record JsonNumber(String string, Number number) implements Token {
        @Override public int length() { return string().length(); }
    };

    record JsonString(String string, String source) implements Token {
        public JsonString(String string) {
            this(string, string);
        }

        public static JsonString decode(String raw) {

            if (raw.contains("\\")) {
                try {
                    var removedOptionalForwardSlashEscapes = raw.replaceAll("\\\\/", "/");
                    var decodedUnicode = decodeUnicode(removedOptionalForwardSlashEscapes);
                    var translatedEscapes = decodedUnicode.translateEscapes();
                    return new JsonString(translatedEscapes, raw);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            return new JsonString(raw, raw);
        }

        public String raw() {
            // For now, let's give back the original...
            return source();
        }

        @Override public int length() { return source.length() + 2; } // +2 for quotation marks
    };


    default int length() {
        return 1;
    }

    record TokenAndTail(Optional<Token> token, String tail) {}

    public static TokenAndTail lex(String json) {
        char c = json.charAt(0);
        try {
            var token = switch (c) {
                case '[' -> Optional.of(BEGIN_ARRAY);
                case ']' -> Optional.of(END_ARRAY);
                case '{' -> Optional.of(BEGIN_OBJECT);
                case '}' -> Optional.of(END_OBJECT);
                case ':' -> Optional.of(NAME_SEPARATOR);
                case ',' -> Optional.of(VALUE_SEPARATOR);
                case 'n','t','f'                                 -> lexLiteral(json);
                case '"'                                         -> lexString(json);
                case '-','0','1','2','3','4','5','6','7','8','9' -> lexNumber(json);
                default  -> Optional.<Token>empty();
            };
            if (token.isPresent()) {
                var tt = new TokenAndTail(token, json.length() > token.get().length() ? json.substring(token.get().length()).trim() : "");

                return tt;
            } else {

                // Whitespace
                // All ok in json.
                // Skip it, until we reach the beginning of a new token.

                if (c != ' ' && c != '\n') {
                    // If we are trying to parse something which isn't json,
                    // for example the string 'game not found' as opposed to the string '{ "message" : "game not found" }',
                    // we will only have unexpected characters here... 'g' 'a' 'm' 'e'... No token matches.

                    //System.out.println("Unexpected character [" + c + "] [" + (int) c + "], skipping!!!!!!!!!!!!!!!!");
                    //Thread.sleep(3000);
                }
                return new TokenAndTail(Optional.empty(), json.substring(1).trim());
            }
        } catch (Exception e) {
            return new TokenAndTail(Optional.empty(), json.length() > 1 ? json.substring(1).trim() : "");
        }
    }

    private static Optional<Token> lexLiteral(String json) {
        if (json.startsWith("true")) {
            return Optional.of(TRUE);
        } else if (json.startsWith("false")) {
            return Optional.of(FALSE);
        } else if (json.startsWith("null")) {
            return Optional.of(NULL);
        }
        return Optional.empty();
    }

    private static Optional<Token> lexString(String json) {
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
                return Optional.of(string);
            }
        } catch (Exception e) {
            // ignore
            e.printStackTrace();
        }

        System.out.println("Couldn't find matching [\"], for json [" + json + "]");
        return Optional.empty();
    }

    private static Optional<Token> lexNumber(String json) {
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
        return Optional.of(new JsonNumber(string, number));
    }

    public static String decodeUnicode(String string) throws Exception {
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

