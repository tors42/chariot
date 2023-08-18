package chariot.internal;

import java.util.Map;

public class ModelMapperUtil {

    // Key is field name in the Java model,
    // and Value is the field name in the JSON model.
    public static Map<String, String> privateMapping() {
        return Map.of("isPrivate", "private");
    }

    public static Map<String, String> intMapping() {
        return Map.of("value", "int");
    }

    public static Map<String, String> importMapping() {
        return Map.of("imported", "import");
    }

    public static Map<String, String> shortMapping() {
        return Map.of("shortname", "short");
    }
}
