package chariot.internal.impl;

import java.time.*;
import java.util.*;
import java.util.function.Consumer;

import chariot.api.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.ChallengeOpenEnded;
import chariot.model.One;

public class ChallengesImpl implements Challenges {

    final RequestHandler requestHandler;

    public ChallengesImpl(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public One<ChallengeOpenEnded> challengeOpenEnded(Consumer<OpenEndedBuilder> consumer) {
        return Endpoint.challengeOpenEnded.newRequest(request -> request
            .body(openEndedBuilderToMap(consumer)))
            .process(requestHandler);
     }

    private Map<String,Object> openEndedBuilderToMap(Consumer<OpenEndedBuilder> consumer) {
        Set<String> rules = new HashSet<>();
        var builder = MapBuilder.of(OpenEndedParams.class)
            .addCustomHandler("users", (args, map) -> map.put("users", args[0] + "," + args[1]))
            .addCustomHandler("expiresIn",  (args, map) -> map.put("expiresAt",
                        Instant.now().plus((Duration)args[0]).toEpochMilli()))
            .addCustomHandler("noAbort",    (args, map) -> rules.add("noAbort"))
            .addCustomHandler("noRematch",  (args, map) -> rules.add("noRematch"))
            .addCustomHandler("noGiveTime", (args, map) -> rules.add("noGiveTime"))
            .addCustomHandler("noClaimWin", (args, map) -> rules.add("noClaimWin"))
            .addCustomHandler("noEarlyDraw", (args, map) -> rules.add("noEarlyDraw"))
            ;

        var openEndedBuilder = new OpenEndedBuilder() {
            @Override
            public OpenEndedParams clock(int initial, int increment) {
                return builder
                    .add("clock.limit", initial)
                    .add("clock.increment", increment)
                    .proxy();
            }
            @Override
            public OpenEndedParams daysPerTurn(int daysPerTurn) {
                return builder
                    .add("days", daysPerTurn)
                    .proxy();
            }
        };
        consumer.accept(openEndedBuilder);
        var map = builder.toMap();
        if (!rules.isEmpty()) map.put("rules", String.join(",", rules));
        return map;
    }
}
