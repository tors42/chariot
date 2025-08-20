package tests.internal.yayson;

import java.util.List;

import chariot.internal.yayson.Lexer;
import chariot.internal.yayson.Token;
import chariot.internal.yayson.Token.*;

import util.Test;

import static util.Assert.*;

public class TestLexer {

    @Test
    public void jsonCompactor() {
        String json = """
        {
            "name": "value"
        }
        """;
        String compact = Lexer.detokenize(Lexer.tokenize(json));
        String expected = """
        {"name":"value"}""";
        assertEquals(expected, compact);
    }

    @Test
    public void emptyString() {
        assertEquals(List.<Token>of(), Lexer.tokenize(""));
    }

    @Test
    public void emptyObject() {
        assertEquals(
                List.of( Token.BEGIN_OBJECT, Token.END_OBJECT),
                Lexer.tokenize("""
                               {}
                               """)
                );
    }

    @Test
    public void emptyArray() {
        String json = """
            []
        """;
        var tokens = Lexer.tokenize(json);
        var expected = List.of(
                Token.BEGIN_ARRAY,
                Token.END_ARRAY
                );
        assertEquals(expected, tokens);
    }

    @Test
    public void nestedEmptyObject() {
        String json = """
            {{}}
        """;
        var tokens = Lexer.tokenize(json);
        var expected = List.of(
                Token.BEGIN_OBJECT,
                Token.BEGIN_OBJECT,
                Token.END_OBJECT,
                Token.END_OBJECT
                );
        assertEquals(expected, tokens);
    }

    @Test
    public void nestedEmptyArray() {
        String json = """
            [[]]
        """;
        var tokens = Lexer.tokenize(json);
        var expected = List.of(
                Token.BEGIN_ARRAY,
                Token.BEGIN_ARRAY,
                Token.END_ARRAY,
                Token.END_ARRAY
                );
        assertEquals(expected, tokens);
    }

    @Test
    public void simpleStringObject() {
        String json = """
            { "key": "value" }
        """;
        var tokens = Lexer.tokenize(json);
        var expected = List.of(
                Token.BEGIN_OBJECT,
                new JsonString("key"),
                Token.NAME_SEPARATOR,
                new JsonString("value"),
                Token.END_OBJECT
                );

        assertEquals(expected, tokens);
    }

    @Test
    public void nestedStringObject() {
        String json = """
            { "key": {
                     "nested_key1": "nested_value1",
                     "nested_key2": "nested_value2"
                }
            }
        """;
        var tokens = Lexer.tokenize(json);

        var expected = List.of(
                Token.BEGIN_OBJECT,
                new Token.JsonString("key"),
                Token.NAME_SEPARATOR,
                Token.BEGIN_OBJECT,
                new Token.JsonString("nested_key1"),
                Token.NAME_SEPARATOR,
                new Token.JsonString("nested_value1"),
                Token.VALUE_SEPARATOR,
                new Token.JsonString("nested_key2"),
                Token.NAME_SEPARATOR,
                new Token.JsonString("nested_value2"),
                Token.END_OBJECT,
                Token.END_OBJECT
                );
        assertEquals(expected, tokens);
    }

    @Test
    public void simpleNumber() {
        String json = """
            { "key": 10 }
        """;
        var tokens = Lexer.tokenize(json);
        var expected = List.of(
                Token.BEGIN_OBJECT,
                new Token.JsonString("key"),
                Token.NAME_SEPARATOR,
                new Token.JsonNumber("10", 10),
                Token.END_OBJECT
                );
        assertEquals(expected, tokens);
    }

    @Test
    public void negativeNumber() {
        String json = """
            { "key": -6 }
        """;
        var tokens = Lexer.tokenize(json);
        var expected = List.of(
                Token.BEGIN_OBJECT,
                new Token.JsonString("key"),
                Token.NAME_SEPARATOR,
                new Token.JsonNumber("-6", -6),
                Token.END_OBJECT
                );
        assertEquals(expected, tokens);
    }

    @Test
    public void fractionalNumber() {
        String json = """
            { "key": 5.5 }
        """;
        var tokens = Lexer.tokenize(json);
        var expected = List.of(
                Token.BEGIN_OBJECT,
                new Token.JsonString("key"),
                Token.NAME_SEPARATOR,
                new Token.JsonNumber("5.5", Float.valueOf(5.5f)),
                Token.END_OBJECT
                );
        assertEquals(expected, tokens);
    }

    @Test
    public void longNumber() {
        String json = """
            { "key": 2147483648 }
        """;
        var tokens = Lexer.tokenize(json);
        var expected = List.of(
                Token.BEGIN_OBJECT,
                new Token.JsonString("key"),
                Token.NAME_SEPARATOR,
                new Token.JsonNumber("2147483648", 2147483648l),
                Token.END_OBJECT
                );
        assertEquals(expected, tokens);
    }

    @Test
    public void nullTest() {
        String json = """
            { "key": null }
        """;
        var tokens = Lexer.tokenize(json);
        var expected = List.of(
                Token.BEGIN_OBJECT,
                new Token.JsonString("key"),
                Token.NAME_SEPARATOR,
                Token.NULL,
                Token.END_OBJECT
                );
        assertEquals(expected, tokens);
    }

    @Test
    public void trueTest() {
        String json = """
            { "key": true }
        """;
        var tokens = Lexer.tokenize(json);
        var expected = List.of(
                Token.BEGIN_OBJECT,
                new Token.JsonString("key"),
                Token.NAME_SEPARATOR,
                Token.TRUE,
                Token.END_OBJECT
                );
        assertEquals(expected, tokens);
    }

    @Test
    public void falseTest() {
        String json = """
            { "key": false }
        """;
        var tokens = Lexer.tokenize(json);
        var expected = List.of(
                Token.BEGIN_OBJECT,
                new Token.JsonString("key"),
                Token.NAME_SEPARATOR,
                Token.FALSE,
                Token.END_OBJECT
                );
        assertEquals(expected, tokens);
    }

    @Test
    public void escapeTest() {
        String json = """
            { "key": "val\nue" }
        """;
        var tokens = Lexer.tokenize(json);
        var expected = List.of(
                Token.BEGIN_OBJECT,
                new Token.JsonString("key"),
                Token.NAME_SEPARATOR,
                new Token.JsonString("val\nue"),
                Token.END_OBJECT
                );
        assertEquals(expected, tokens);
    }

    @Test
    public void weirdChar() {
        String json = """
          { "description" : "⛔" }
            """;
        var tokens = Lexer.tokenize(json);
        var expected = List.of(
                Token.BEGIN_OBJECT,
                new Token.JsonString("description"),
                Token.NAME_SEPARATOR,
                new Token.JsonString("⛔"),
                Token.END_OBJECT
                );
        assertEquals(expected, tokens);
     }

    @Test
    public void doubleChar() {
        String json = """
          { "description" : "\uD83D\uDCDC" }
            """;
        var tokens = Lexer.tokenize(json);
        var expected = List.of(
                Token.BEGIN_OBJECT,
                new Token.JsonString("description"),
                Token.NAME_SEPARATOR,
                new Token.JsonString("📜"),
                Token.END_OBJECT
                );
        assertEquals(expected, tokens);
     }

}

