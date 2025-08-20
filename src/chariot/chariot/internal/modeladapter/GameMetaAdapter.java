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
        TimeControl time = nodeToTimeControl(gameMetaYo);
        Enums.Status status = Enums.Status.valueOf(gameMetaYo.getInteger("status"));
        ZonedDateTime createdAt = Util.fromLong(gameMetaYo.getLong("createdAt"));
        GameMeta.Players players = mapper.fromYayTree(gameMetaYo.value().get("players"), GameMeta.Players.class);
        Opt<Enums.Color> winner = gameMetaYo.getString("winner") instanceof String color ? Opt.of(Enums.Color.valueOf(color)) : Opt.of();

        return new GameMeta(id, rated, variant, time, status, createdAt, players, winner);
    }

    static GameMeta.Player nodeToPlayer(YayNode node) {
        return switch (node) {
            case YayObject yo when
                yo.getNumber("ai") instanceof Integer aiLevel
                -> new GameMeta.AI(aiLevel);
            case YayObject yo when
                yo.getString("userId") instanceof String userId &&
                yo.getInteger("rating") instanceof Integer rating
                -> new GameMeta.Account(userId, rating, yo.getBool("provisional"));
            case null, default -> new GameMeta.Anonymous();
        };
    }

    static TimeControl nodeToTimeControl(YayObject gameMetaYo) {
        return switch (gameMetaYo) {
            case YayObject gmy
                when gameMetaYo.value().get("clock") instanceof YayNode clockNode
                     && gameMetaYo.getString("speed") instanceof String speed
                -> nodeToRealTime(clockNode, speed);
            case YayObject gmy
                when gameMetaYo.getInteger("daysPerTurn") instanceof Integer daysPerTurn
                -> new Correspondence(daysPerTurn);
            default -> new Unlimited();
        };
    }

    static RealTime nodeToRealTime(YayNode node, String speed) {
        Clock clock = nodeToClock(node);
        return new RealTime(clock, clock.toString(), Speed.valueOf(speed));
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
