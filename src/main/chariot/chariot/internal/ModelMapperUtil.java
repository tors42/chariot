package chariot.internal;

import java.util.Map;
import java.util.stream.Collectors;

import chariot.model.Enums.PerfTypeNoCorr;

import java.util.Arrays;
import java.util.HashMap;

public class ModelMapperUtil {

    // Key is field name in the Java model,
    // and Value is the field name in the JSON model.
    public static Map<String, String> privateMapping() {
        return Map.of("isPrivate", "private");
    }

    public static Map<String, String> intMapping() {
        return Map.of("value", "int");
    }

    public static Map<String, String> createdAtAndLastMoveAtMapping() {
        return Map.of("createdTime",  "createdAt",
                      "lastMoveTime", "lastMoveAt");
    }

    public static Map<String, String> createdAtMapping() {
        return Map.of("createdTime",  "createdAt");
    }

    public static Map<String, String> startsAtMapping() {
        return Map.of("startsTime",  "startsAt");
    }


    public static Map<String, String> tvChannelsMapping() {

        var perfTypeMap = Arrays.stream(PerfTypeNoCorr.values()).collect(
                Collectors.toMap(
                    p -> p.name(),
                    p -> switch(p) {
                        case ultraBullet,
                             bullet,
                             blitz,
                             rapid,
                             classical,
                             antichess,
                             horde,
                             atomic,
                             crazyhouse,
                             chess960      -> capitalize(p.name());
                        case racingKings   -> "Racing Kings";
                        case threeCheck    -> "Three-check";
                        case kingOfTheHill -> "King of the Hill";
                    })
                );

        var map = new HashMap<String, String>() {{
            putAll(perfTypeMap);
            put("topRated", "Top Rated");
            put("computer", "Computer");
            put("bot", "Bot");
        }};

        return map;
    }

    public static Map<String, String> importMapping() {
        return Map.of("imported", "import");
    }

    public static Map<String, String> shortMapping() {
        return Map.of("shortname", "short");
    }

    private static String capitalize(String string) {
        return String.valueOf(string.charAt(0)).toUpperCase() + string.substring(1);
    }

}
