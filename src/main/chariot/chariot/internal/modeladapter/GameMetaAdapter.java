package chariot.internal.modeladapter;

import java.time.*;

import chariot.internal.Util;
import chariot.internal.yayson.Parser.*;
import chariot.internal.yayson.YayMapper;
import chariot.model.Enums.*;
import chariot.model.Clock;
import chariot.model.*;

public interface GameMetaAdapter {

    static GameMeta nodeToGameMeta(YayNode node, YayMapper mapper) {
        if (! (node instanceof YayObject gameMetaYo)) return null;

        String id = gameMetaYo.getString("id");
        boolean rated = gameMetaYo.getBool("rated");
        Variant variant = toVariant(gameMetaYo);
        RealTime time = nodeToRealTime(gameMetaYo);
        Enums.Status status = Enums.Status.valueOf(gameMetaYo.getInteger("status"));
        ZonedDateTime createdAt = Util.fromLong(gameMetaYo.getLong("createdAt"));
        GameMeta.Players players = mapper.fromYayTree(gameMetaYo.value().get("players"), GameMeta.Players.class);
        Opt<Enums.Color> winner = gameMetaYo.getString("winner") instanceof String color ? Opt.of(Enums.Color.valueOf(color)) : Opt.of();

        return new GameMeta(id, rated, variant, time, status, createdAt, players, winner);
    }

    static RealTime nodeToRealTime(YayObject gameMetaYo) {
        Clock clock = nodeToClock(gameMetaYo.value().get("clock"));
        return new RealTime(clock, clock.toString(), Speed.valueOf(gameMetaYo.getString("speed")));
    }

    static Clock nodeToClock(YayNode node) {
        return switch(node) {
            case YayObject clockYo
                when clockYo.getInteger("initial") instanceof Integer initial
                && clockYo.getInteger("increment") instanceof Integer increment
                -> Clock.ofSeconds(initial).withIncrementSeconds(increment);
            default -> Clock.of(Duration.ZERO);
        };
    }

    static Variant toVariant(YayNode node) {
        return switch(node) {
            case YayObject yo when yo.getString("variant") instanceof String variant ->
                switch(variant) {
                    case "chess960"     -> new Variant.Chess960(Opt.of(yo.getString("initialFen")));
                    case "fromPosition" -> new Variant.FromPosition(Opt.of(yo.getString("initialFen")));
                    default -> Variant.Basic.valueOf(variant);
                };
            default -> Variant.Basic.standard;
        };
    }
}
