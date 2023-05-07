package tests.internal.yayson;

import chariot.internal.yayson.Parser;
import chariot.internal.yayson.Parser.*;
import util.Test;

import static util.Assert.*;

public class TestParser {

    @Test
    public void emptyString() {
        String json = "";
        var node = Parser.fromString(json);
        if (! (node instanceof YayEmpty)) {
            fail();
        }
    }

    @Test
    public void emptyObject() {
        String json = """
            {}
        """;
        var node = Parser.fromString(json);
        assertEquals(node.getClass(), YayObject.class);
    }

    @Test
    public void emptyArray() {
        String json = """
            []
        """;
        var node = Parser.fromString(json);
        assertEquals(node.getClass(), YayArray.class);
    }

    @Test
    public void nestedEmptyArray() {
        String json = """
            [[]]
        """;
        var node = Parser.fromString(json);
        assertEquals(node.getClass(), YayArray.class);
    }

    @Test
    public void simpleStringObject() {
        String json = """
            { "simple": "test" }
        """;
        var node = Parser.fromString(json);
        assertNotNull(node);
        if (node instanceof YayObject yo) {
            assertEquals("test", yo.getString("simple"));
        } else {
            fail();
        }
    }

    @Test
    public void escapedStringObject() {
        String json = """
            {"key": "value with \\"quotes\\" in it"}
        """;

        var node = Parser.fromString(json);
        assertNotNull(node);
        if (node instanceof YayObject yo) {
            assertEquals("value with \"quotes\" in it", yo.getString("key"));
        } else {
            fail();
        }
    }


    @Test
    public void escapedLineBreak() {
        String json = """
            {
                "key": "value with newline\r\nand then some"
            }
        """;
        var node = Parser.fromString(json);
        assertNotNull(node);
        if (node instanceof YayObject yo) {
            assertEquals("value with newline\r\nand then some", yo.getString("key"));
        } else {
            fail();
        }
    }


    @Test
    public void nestedStringObject() {
        String json = """
            { "key": {
                     "nested_key1":        "nested_value1",
                     "nested_key2":  "nested_value2"
                }
            }
        """;
        var node = Parser.fromString(json);
        assertNotNull(node);
    }

    @Test
    public void simpleNumber() {
        String json = """
            { "key": 10 }
        """;
        var node = Parser.fromString(json);
        if (node instanceof YayObject yo) {
            assertEquals(10, yo.getNumber("key"));
        } else {
            fail();
        }
    }

    @Test
    public void negativeNumber() {
        String json = """
            { "key": -6 }
        """;
        var node = Parser.fromString(json);
        if (node instanceof YayObject yo) {
            assertEquals(-6, yo.getNumber("key"));
        } else {
            fail();
        }
    }

    @Test
    public void fractionalNumber() {
        String json = """
            { "key": 5.5 }
        """;
        var node = Parser.fromString(json);
        if (node instanceof YayObject yo) {
            assertEquals(5.5f, yo.getNumber("key"));
        } else {
            fail();
        }
    }

    @Test
    public void longNumber() {
        String json = """
            { "key": 2147483648 }
        """;
        var node = Parser.fromString(json);
        if (node instanceof YayObject yo) {
            assertEquals(2147483648l, yo.getNumber("key"));
        } else {
            fail();
        }
    }

    @Test
    public void nullTest() {
        String json = """
            { "key": null }
        """;
        var node = Parser.fromString(json);
        if (! (node instanceof YayObject yo)) {
            fail();
        } else {
            if (! (yo.value().get("key") instanceof YayNull)) {
                fail();
            }
        }
    }

    @Test
    public void trueTest() {
        String json = """
            { "key": true }
        """;
        var node = Parser.fromString(json);
        if (node instanceof YayObject yo) {
            assertTrue(yo.getBool("key"));
        } else {
            fail();
        }
    }

    @Test
    public void falseTest() {
        String json = """
            { "key": false }
        """;
        var node = Parser.fromString(json);
        if (node instanceof YayObject yo) {
            assertFalse(yo.getBool("key"));
        } else {
            fail();
        }
    }

    @Test
    public void weirdStringObject() {
        String json = """
            {
                "key": "some weird ♞(ง︡'-'︠)ง♞ ",
                "key2": "value2"
            }
        """;
        var node = Parser.fromString(json);
        assertNotNull(node);
        if (node instanceof YayObject yo) {
            assertEquals("some weird ♞(ง︡'-'︠)ง♞ ", yo.getString("key"));
            assertEquals("value2", yo.getString("key2"));
        } else {
            fail();
        }
    }
}
