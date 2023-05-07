package chariot.internal;

import java.util.Map;
import java.util.stream.Collectors;

import chariot.model.Enums.PerfTypeNoCorr;

import java.util.*;
import java.util.stream.Stream;

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
        return Stream.concat(Arrays.stream(PerfTypeNoCorr.values())
                .map(perfType -> Map.entry(perfType.name(), switch(perfType) {
                    case ultraBullet,
                         bullet,
                         blitz,
                         rapid,
                         classical,
                         antichess,
                         horde,
                         atomic,
                         crazyhouse,
                         chess960      -> capitalize(perfType.name());
                    case racingKings   -> "Racing Kings";
                    case threeCheck    -> "Three-check";
                    case kingOfTheHill -> "King of the Hill";
                })),
                Stream.of(
                    Map.entry("topRated", "Top Rated"),
                    Map.entry("computer", "Computer"),
                    Map.entry("bot", "Bot"))
                ).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey,  Map.Entry::getValue));
    }

    public static Map<String, String> importMapping() {
        return Map.of("imported", "import");
    }

    public static Map<String, String> shortMapping() {
        return Map.of("shortname", "short");
    }

    private static String capitalize(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

}
