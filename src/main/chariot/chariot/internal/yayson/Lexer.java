package chariot.internal.yayson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Lexer {
    public static List<Token> tokenize(String json) {
        Objects.requireNonNull(json);
        json = json.trim();
        var tokens = new ArrayList<Token>();
        while (json.length() > 0) {
            var tt = Token.lex(json);
            tt.token().ifPresent(t -> tokens.add(t));
            json = tt.tail();
        }
        return tokens;
    }

    // --enable-preview
    // .map( t -> switch (t) { ... } ).collect()
    public static String detokenize(List<Token> tokens) {
        return tokens.stream()
            .map(t -> {
                if (t instanceof Token.BeginArray ba) {
                    return String.valueOf(ba.c());
                } else if (t instanceof Token.BeginObject bo) {
                    return String.valueOf(bo.c());
                } else if (t instanceof Token.EndArray ea) {
                    return String.valueOf(ea.c());
                } else if (t instanceof Token.EndObject eo) {
                    return String.valueOf(eo.c());
                } else if (t instanceof Token.NameSeparator ns) {
                    return String.valueOf(ns.c());
                } else if (t instanceof Token.ValueSeparator vs) {
                    return String.valueOf(vs.c());
                } else if (t instanceof Token.False) {
                    return "false";
                } else if (t instanceof Token.True) {
                    return "true";
                } else if (t instanceof Token.Null) {
                    return "null";
                } else if (t instanceof Token.JsonNumber n) {
                    return n.string();
                } else if (t instanceof Token.JsonString s) {
                    return '"' + s.raw() + '"';
                } else {
                    return "<Unknown Token [%t]>".formatted(t);
                }
            }
        ).collect(Collectors.joining());
    }
}
