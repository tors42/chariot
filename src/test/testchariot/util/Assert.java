package util;

import java.lang.StackWalker.Option;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface Assert {

    static void assertEquals(Object expected, Object actual) {
        assertEquals(expected, actual, Util.formatExpectedActual(expected, actual));
    }

    static void assertEquals(Object expected, Object actual, String message) {
        assertEquals(expected, actual, () -> message);
    }
    static void assertEquals(Object expected, Object actual, Supplier<Object> message) {
        _assert(Objects.equals(expected, actual), message);
    }

    static void assertTrue(boolean b)                            { assertEquals(true, b); }
    static void assertTrue(boolean b, String message)            { assertEquals(true, b, () -> message); }
    static void assertTrue(boolean b, Supplier<Object> message)  { assertEquals(true, b, message); }
    static void assertFalse(boolean b)                           { assertEquals(false, b); }
    static void assertFalse(boolean b, String message)           { assertEquals(false, b, () -> message); }
    static void assertFalse(boolean b, Supplier<Object> message) { assertEquals(false, b, message); }

    static void assertNotNull(Object o)                           { assertNotNull(o, "Expected non-null but got null"); }
    static void assertNotNull(Object o, String message)           { assertNotNull(o, () -> message); }
    static void assertNotNull(Object o, Supplier<Object> message) { _assert(o != null, message); }

    static void fail()                         { fail(() -> "fail"); }
    static void fail(Object message)           { fail(() -> String.valueOf(message)); }
    static void fail(Supplier<Object> message) { _fail(message); }

    private static void _assert(boolean bool, Supplier<Object> message) {
        if (! bool) {
            _fail(message);
        } else {
            results.add(new Success(sourceTestCase()));
        }
    }

    private static void _fail(Supplier<Object> message) {
        results.add(new Failure(sourceTestCase(), "\n"+String.valueOf(message.get())));
    }

    sealed interface Result {}
    record Success(String test) implements Result {}
    record Failure(String test, String message) implements Result {}

    final static List<Result> results = new ArrayList<>();

    final static StackWalker stackWalker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    public static String sourceTestCase() {
        return stackWalker.walk(stackframes -> stackframes
                .filter(sf -> ! sf.getClassName().equals(Assert.class.getName()))
                //.peek(sf -> System.out.println(sf.getDeclaringClass().getName()))
                .filter(sf -> {
                    try {
                        var method = sf.getDeclaringClass().getDeclaredMethod(sf.getMethodName());
                        return method.isAnnotationPresent(Test.class) || method.isAnnotationPresent(IntegrationTest.class);
                    } catch (Exception ex) {}
                    return false;
                })
                .map(sf -> "%s.%s".formatted(sf.getClassName(), sf.getMethodName()))
                //.map(sf -> sf.startsWith("src.test.testchariot.") ? sf.substring("src.test.testchariot.".length()) : sf)
                .findFirst()
                .orElse(""));
    }

    static <T> Stream<T> filterCast(Stream<? super T> stream, Class<T> cls) {
        return stream.filter(cls::isInstance).map(cls::cast);
    }
}
