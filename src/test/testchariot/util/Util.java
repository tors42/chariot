package util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import chariot.internal.yayson.Lexer;

public interface Util {

    static String formatExpectedActual(Object expected, Object actual) {
        return """
            [Expected]
            [%s]
            [Actual]
            [%s]
            """.formatted(expected, actual);
    }


    static String compactJson(String json) {
        try {
            return Lexer.detokenize(Lexer.tokenize(json));
        } catch (Exception e) {
            return """
            { "error" : "Failed to reparse - %s" }
            """.formatted(e.getMessage());
        }
    }

    static String urlEncode(String data) {
        return URLEncoder.encode(data, StandardCharsets.UTF_8);
    }


}
