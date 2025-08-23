package chariot.internal.yayson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import chariot.internal.yayson.Token.*;

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

    public static String detokenize(List<Token> tokens) {
        return tokens.stream()
            .map(t -> switch(t) {
                case Structural structural  -> String.valueOf(structural.token());
                case Literal literal        -> literal.literal();
                case JsonNumber(var str, _) -> str;
                case JsonString(_, var src) -> '"' + src + '"';
            })
        .collect(Collectors.joining());
    }
}
