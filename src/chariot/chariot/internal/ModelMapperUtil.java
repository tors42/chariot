package chariot.internal;

import java.util.*;

public class ModelMapperUtil {

    // Key is field name in the Java model,
    // and Value is the field name in the JSON model.
    static Map<String, String> privateMapping() {
        return Map.of("isPrivate", "private");
    }

    static Map<String, String> intMapping() {
        return Map.of("value", "int");
    }

    static Map<String, String> importMapping() {
        return Map.of("imported", "import");
    }

    static Map<String, String> shortMapping() {
        return Map.of("shortname", "short");
    }

}
