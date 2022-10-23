package chariot.internal.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.*;

import chariot.Client.Scope;
import chariot.api.*;
import chariot.model.*;
import chariot.model.Enums.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;

public class ChallengesAuthCommonImpl extends ChallengesImpl implements ChallengesAuthCommon {

    private final Scope scope;

    public ChallengesAuthCommonImpl(InternalClient client, Scope scope) {
        super(client);
        this.scope = scope;
    }

    @Override
    public Many<Event> connect() {
        return connect(scope);
    }

    @Override
    public One<Challenge> challenge(String userId, Consumer<ChallengeBuilder> parameters) {
        return challenge(scope, userId, parameters);
    }

    @Override
    public Many<Challenge> challengeKeepAlive(String userId, Consumer<ChallengeBuilder> parameters) {
        return challengeKeepAlive(scope, userId, parameters);
    }

    @Override
    public One<ChallengeAI> challengeAI(Consumer<ChallengeAIBuilder> parameters) {
        return challengeAI(scope, parameters);
    }

    @Override
    public One<Ack> cancelChallenge(String challengeId, Supplier<char[]> opponentToken) {
        return cancelChallenge(scope, challengeId, opponentToken);
    }

    @Override
    public One<Ack> cancelChallenge(String challengeId) {
        return cancelChallenge(scope, challengeId);
    }

    @Override
    public One<Ack> acceptChallenge(String challengeId) {
        return acceptChallenge(scope, challengeId);
    }

    @Override
    public One<Ack> declineChallenge(String challengeId, DeclineReason reason) {
        return declineChallenge(scope, challengeId, reason);
    }

    @Override
    public One<Ack> declineChallenge(String challengeId) {
        return declineChallenge(scope, challengeId);
    }


    private Many<Event> connect(Scope scope) {
        return Endpoint.streamEvents.newRequest(request -> request
                .scope(scope)
                .stream())
            .process(this);
    }

    private One<Challenge> challenge(Scope scope, String userId, Consumer<ChallengeBuilder> consumer) {
        return Endpoint.challengeCreate.newRequest(request -> request
                .scope(scope)
                .path(userId)
                .body(challengeBuilderToMap(consumer)))
            .process(this);
    }

    private Many<Challenge> challengeKeepAlive(Scope scope, String userId, Consumer<ChallengeBuilder> consumer) {
        var map = challengeBuilderToMap(consumer);
        map.put("keepAliveStream", Boolean.valueOf(true));
        return Endpoint.challengeCreateKeepAlive.newRequest(request -> request
                .scope(scope)
                .path(userId)
                .body(map)
                .stream())
            .process(this);
    }

    private One<ChallengeAI> challengeAI(Scope scope, Consumer<ChallengeAIBuilder> consumer) {
        return Endpoint.challengeAI.newRequest(request -> request
                .scope(scope)
                .body(challengeAIBuilderToMap(consumer)))
            .process(this);
    }

    private One<Ack> cancelChallenge(Scope scope, String challengeId, Supplier<char[]> opponentToken) {
        return Endpoint.challengeCancel.newRequest(request -> request
                .scope(scope)
                .path(challengeId)
                .query(Map.of("opponentToken", String.valueOf(opponentToken.get()))))
            .process(this);
    }

    private One<Ack> cancelChallenge(Scope scope, String challengeId) {
        return Endpoint.challengeCancel.newRequest(request -> request
                .scope(scope)
                .path(challengeId))
            .process(this);
    }

    private One<Ack> acceptChallenge(Scope scope, String challengeId) {
        return Endpoint.challengeAccept.newRequest(request -> request
                .scope(scope)
                .path(challengeId))
            .process(this);
    }

    private One<Ack> declineChallenge(Scope scope, String challengeId, DeclineReason reason) {
        return Endpoint.challengeDecline.newRequest(request -> request
                .body(Map.of("reason", reason.name()))
                .path(challengeId))
            .process(this);
    }

    private One<Ack> declineChallenge(Scope scope, String challengeId) {
        return Endpoint.challengeDecline.newRequest(request -> request
                .path(challengeId))
            .process(this);
    }

    private Map<String, Object> challengeBuilderToMap(Consumer<ChallengeBuilder> consumer) {
        Set<String> rules = new HashSet<>();
        var builder = MapBuilder.of(ChallengeParams.class)
            .addCustomHandler("acceptByToken", (args, map) -> {
                map.put("acceptByToken", args[0]);
                if (args.length == 2) map.put("message", args[1]);
            })
            .addCustomHandler("noAbort",    (args, map) -> rules.add("noAbort"))
            .addCustomHandler("noRematch",  (args, map) -> rules.add("noRematch"))
            .addCustomHandler("noGiveTime", (args, map) -> rules.add("noGiveTime"))
            .addCustomHandler("noClaimWin", (args, map) -> rules.add("noClaimWin"));

        var challengeBuilder = new ChallengeBuilder() {
            @Override
            public ChallengeParams clock(int initial, int increment) {
                return builder
                    .add("clock.limit", initial)
                    .add("clock.increment", increment)
                    .proxy();
            }

            @Override
            public ChallengeParams daysPerTurn(int daysPerTurn) {
                return builder
                    .add("days", daysPerTurn)
                    .proxy();
             }
        };
        consumer.accept(challengeBuilder);
        var map = builder.toMap();
        if (!rules.isEmpty()) map.put("rules", String.join(",", rules));
        return map;
    }

    private Map<String, Object> challengeAIBuilderToMap(Consumer<ChallengeAIBuilder> consumer) {
        var builder = MapBuilder.of(ChallengeAIParams.class)
            .addCustomHandler("level", (args, map) -> map.put("level", Level.class.cast(args[0]).level) )
            .add("level","1");

        var challengeBuilder = new ChallengeAIBuilder() {
            @Override
            public ChallengeAIParams clock(int initial, int increment) {
                return builder
                    .add("clock.limit", initial)
                    .add("clock.increment", increment)
                    .proxy();
            }

            @Override
            public ChallengeAIParams daysPerTurn(int daysPerTurn) {
                return builder
                    .add("days", daysPerTurn)
                    .proxy();
             }
        };
        consumer.accept(challengeBuilder);
        return builder.toMap();
    }
}
