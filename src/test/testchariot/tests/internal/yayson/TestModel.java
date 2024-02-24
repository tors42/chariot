package tests.internal.yayson;

import java.time.ZonedDateTime;
import java.util.*;

import chariot.internal.Util;
import chariot.internal.yayson.YayMapper;
import util.Test;

import static util.Assert.*;

public class TestModel {

    private final static YayMapper mapper = YayMapper.mapper();

    @Test
    public void parseEmptyRecord() {
        var json = "{}";
        var v = mapper.fromString(json, Empty.class);
        assertNotNull(v);
    }


    @Test
    public void parseSimple() {
        var jsontrue = """
            {
                "b": true
            }
            """;
        var jsonfalse = """
            {
                "b": false
            }
            """;

        SimpleBool vtrue = mapper.fromString(jsontrue, SimpleBool.class);
        SimpleBool vfalse = mapper.fromString(jsonfalse, SimpleBool.class);

        assertTrue(vtrue.b());
        assertFalse(vfalse.b());
    }

    @Test
    public void parseDifferentTypes() {

        var json = """
            {
                "bt": true,
                "Bt": true,
                "bf": false,
                "Bf": false,
                "s": "Some kind of text",
                "i": 10,
                "I": 20,
                "f": 1.1,
                "F": 2.2,
                "null": null,
                "nest": {
                    "key": "value"
                },
                "arrbool": [
                    true,
                    true
                ],
                "arrBool": [
                    true,
                    true
                ],
                "listBool": [
                    true,
                    true
                ],
                "map": {
                    "nest1": {
                        "key": "value1"
                    },
                    "nest2": {
                        "key": "value2"
                    }
                },
                "nestArr": [
                    { "key": "arrvalue1" },
                    { "key": "arrvalue2" }
                ],
                "nestList": [
                    { "key": "listvalue1" },
                    { "key": "listvalue2" }
                ]
             }
            """;

        DifferentTypes types = mapper.fromString(json, DifferentTypes.class);

        assertNotNull(types);

        assertEquals(true, types.bt());
        assertEquals(true, types.Bt());
        assertEquals(false, types.bf());
        assertEquals(false, types.Bf());
        assertEquals("Some kind of text", types.s());
        assertEquals(10, types.i());
        assertEquals(20, types.I());
        assertEquals(1.1f, types.f());
        assertEquals(2.2f, types.F());
        assertEquals(null, types.nullobject());

        Nest nest = types.nest();
        assertNotNull(nest);
        assertEquals(nest.key(), "value");

        boolean[] arrbool = types.arrbool();
        assertNotNull(arrbool);
        assertEquals(2, arrbool.length);
        assertTrue(arrbool[0]);
        assertTrue(arrbool[1]);

        Boolean[] arrBool = types.arrBool();
        assertNotNull(arrBool);
        assertEquals(2, arrBool.length);
        assertTrue(arrBool[0]);
        assertTrue(arrBool[1]);

        List<Boolean> listBool = types.listBool();
        assertNotNull(listBool);
        assertEquals(2, listBool.size());
        assertTrue(listBool.get(0));
        assertTrue(listBool.get(1));

        Map<String, Nest> map = types.map();
        assertNotNull(map);
        Nest nest1 = map.get("nest1");
        assertNotNull(nest1);
        assertEquals("value1", nest1.key());
        Nest nest2 = map.get("nest2");
        assertNotNull(nest2);
        assertEquals("value2", nest2.key());

        Nest[] nestArr = types.nestArr();
        assertNotNull(nestArr);
        assertEquals(2, nestArr.length);
        assertEquals("arrvalue1", nestArr[0].key());
        assertEquals("arrvalue2", nestArr[1].key());

        List<Nest> nestList = types.nestList();
        assertNotNull(nestList);
        assertEquals(2, nestList.size());
        assertEquals("listvalue1", nestList.get(0).key());
        assertEquals("listvalue2", nestList.get(1).key());
    }

    @Test
    public void parseNested() {
        var json = """
            {
                "b": true,
                "nest": {
                    "s": "The nested string"
                },
                "s": "The top string"
            }
            """;

        Top top = mapper.fromString(json, Top.class);

        assertNotNull(top);

        assertEquals(true, top.b());
        assertEquals("The top string", top.s());

        Nested nest = top.nest();
        assertNotNull(nest);
        assertEquals("The nested string", nest.s());
    }

    @Test
    public void parseMappings() {

        var json = """
            {
                "protected": "value1",
                "boolean": true,
                "normal": "value2"
            }
            """;

        MappedName mappedFailure = mapper.fromString(json, MappedName.class);

        assertEquals(null, mappedFailure.protectedKeyword());

        // Hmm, is it desired to default "booleanKeyword" of primitive type boolean to false,
        // when it wasn't found in the json?
        // Should it fail the parsing instead?
        // If one is okay with missing field, one use Boolean object instead?
        // Hmm...
        assertFalse(mappedFailure.booleanKeyword());

        assertEquals("value2", mappedFailure.normal());

        mapper.setMappings(MappedName.class, Map.of(
                    "protectedKeyword", "protected",
                    "booleanKeyword", "boolean"
                    ));

        MappedName mappedSuccess = mapper.fromString(json, MappedName.class);

        assertEquals("value1", mappedSuccess.protectedKeyword());
        assertTrue(mappedSuccess.booleanKeyword());
        assertEquals("value2", mappedSuccess.normal());
    }

    @Test
    public void parseSealedInterface() {

        var jsonThis = """
            {
                "something": {
                    "string": "some string"
                }
            }
            """;
        var jsonThat = """
            {
                "something": {
                    "integer": 42
                }
            }
            """;

        var wrapThis = mapper.fromString(jsonThis, Wrapper.class);
        var wrapThat = mapper.fromString(jsonThat, Wrapper.class);

        assertNotNull(wrapThis);
        assertNotNull(wrapThat);

        var someThis = wrapThis.something();
        var someThat = wrapThat.something();
        assertNotNull(someThis);
        assertNotNull(someThat);

        if (someThis instanceof This t) {
            assertEquals("some string", t.string());
        } else {
            fail("Expected a This but was " + someThis);
        }
        if (someThat instanceof That t) {
            assertEquals(42, t.integer());
        } else {
            fail("Expected a That but was " + someThat);
        }
    }

    static sealed interface Fields {}
    public record One(int field1) implements Fields {}
    public record Two(int field1, int field2) implements Fields {}
    public record Three(int field1, int field2, int field3) implements Fields {}

    @Test
    public void testBestMatch() {
        var jsonField1Field2Field3 = """
            {
                "field1": 1,
                "field2": 2,
                "field3": 3
            }
            """;
        assertTrue(mapper.fromString(jsonField1Field2Field3, Fields.class) instanceof Three(int f1, int f2, int f3));
    }

    @Test
    public void parseEnum() {
        var json = """
            {
                "id": "abcdefgh",
                "winner": "black"
            }""";
        var mapped = mapper.fromString(json, Game.class);
        assertEquals(mapped.id(), "abcdefgh");
        assertEquals(mapped.winner().toString(), "black");
    }

    @Test
    public void parseOptional() {
        var jsonWithTrueField = """
            {
                "id": "abcdefgh",
                "someField": true
            }""";

        var jsonWithFalseField = """
            {
                "id": "abcdefgh",
                "someField": false
            }""";

        var jsonWithoutField = """
            {
                "id": "abcdefgh"
            }""";

        var mappedWithTrueField = mapper.fromString(jsonWithTrueField, GameWithOpt.class);
        var mappedWithFalseField = mapper.fromString(jsonWithFalseField, GameWithOpt.class);
        var mappedWithoutField = mapper.fromString(jsonWithoutField, GameWithOpt.class);

        assertTrue(mappedWithTrueField.someField().isPresent());
        assertTrue(mappedWithFalseField.someField().isPresent());
        assertFalse(mappedWithoutField.someField().isPresent());

        assertTrue(mappedWithTrueField.someField().get().booleanValue());
        assertFalse(mappedWithFalseField.someField().get().booleanValue());
    }

    @Test
    public void parseZonedDateTimeFromLong() {
        var jsonWithLongField = """
            {
                "id": "abcdefgh",
                "createdAt": 1463756350225
            }""";

        String id = "abcdefgh";
        long createdAt = 1463756350225l;

        var expected  = new ZDT(id, Util.fromLong(createdAt));

        var actual = mapper.fromString(jsonWithLongField, ZDT.class);

        assertEquals(expected, actual);
    }

    public record ZDT(String id, ZonedDateTime createdAt) {}

    public record Empty() {};
    public record SimpleBool(boolean b) {};
    public record Nest(String key) {}

    public record DifferentTypes(
             boolean bt,
             Boolean Bt,
             boolean bf,
             Boolean Bf,
             String s,
             int i,
             Integer I,
             float f,
             Float F,
             Object nullobject,
             Nest nest,
             boolean[] arrbool,
             Boolean[] arrBool,
             List<Boolean> listBool,
             Map<String, Nest> map,
             Nest[] nestArr,
             List<Nest> nestList
             ) {};

    public record Nested(String s) {}
    public record Top(boolean b, Nested nest, String s) {};
    public record MappedName(String protectedKeyword, boolean booleanKeyword, String normal) {};
    public record Wrapper(Something something) {}
    public sealed interface Something permits This, That {}
    public record This(String string) implements Something {}
    public record That(Integer integer) implements Something {}
    public record GameWithOpt(String id, Optional<Boolean> someField) {}
    public record Game(String id, Winner winner) {}

    public enum Winner { white, black; };

}
