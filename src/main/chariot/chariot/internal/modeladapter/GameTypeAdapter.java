package chariot.internal.modeladapter;

import java.time.Duration;

import chariot.internal.yayson.Parser.*;
import chariot.model.Enums.*;
import chariot.model.*;

public interface GameTypeAdapter {
    /**
     * <pre>
     * </pre>
     *
     */
    static GameType nodeToGameType(YayNode node) {
        return switch(node) {
            case YayObject yo -> new GameType(
                    yo.getBool("rated"),
                    nodeToVariant(yo.value().get("variant"), yo.getString("initialFen")),
                    nodeToTimeControl(yo));
            default -> null;
        };
    }

    private static Variant nodeToVariant(YayNode node, String initialFen) {
        return switch(node) {
            case YayObject variantYo when variantYo.getString("key") instanceof String key
                -> switch(key) {
                    case "chess960"     -> new Variant.Chess960(Opt.of(initialFen));
                    case "fromPosition" -> new Variant.FromPosition(Opt.of(initialFen));
                    default             -> Variant.Basic.valueOf(key);
                };
            default -> null;
        };
    }

    private static TimeControl nodeToTimeControl(YayNode node) {
        return switch(node) {
            case YayObject yo when yo.value().get("timeControl") instanceof YayObject timeYo
                -> switch(timeYo.getString("type")) {
                    case "unlimited"      -> new Unlimited();
                    case "correspondence" -> new Correspondence(timeYo.getInteger("daysPerTurn"));
                    case "clock"          -> new RealTime(
                                                new Clock(
                                                    Duration.ofSeconds(timeYo.getInteger("limit")),
                                                    Duration.ofSeconds(timeYo.getInteger("increment"))),
                                                timeYo.getString("show"),
                                                Speed.valueOf(yo.getString("speed")));
                    case null, default    -> null;
                };
            case YayObject yo when yo.value().get("clock") instanceof YayObject clockYo
                -> {
                    var initial = Duration.ofMillis(clockYo.getLong("initial"));
                    var increment = Duration.ofMillis(clockYo.getLong("increment"));
                    yield new RealTime(new Clock(initial, increment),
                            "%d+%d".formatted(initial.toMinutes(), increment.toSeconds()),
                            Speed.valueOf(yo.getString("speed")));
                }
            case YayObject yo when yo.value().get("daysPerTurn") instanceof YayNumber(var number)
                -> new Correspondence(number.intValue());
            case null, default
                -> new Unlimited();
        };
    }
}
