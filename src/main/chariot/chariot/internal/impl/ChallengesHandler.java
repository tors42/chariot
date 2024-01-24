package chariot.internal.impl;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import chariot.Client.Scope;
import chariot.api.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.*;

public class ChallengesHandler extends ChallengesAuthCommonImpl implements ChallengesAuth {

    public ChallengesHandler(RequestHandler requestHandler) {
        super(requestHandler, Scope.challenge_write);
    }

    @Override
    public One<PendingChallenges> challenges() {
        return Endpoint.challenges.newRequest(request -> {})
            .process(requestHandler);
    }

    @Override
    public One<Void> startClocksOfGame(String gameId, String token1, String token2) {
        return Endpoint.startClocksOfGame.newRequest(request -> request
                .path(gameId)
                .query(Map.of("token1", token1, "token2", token2)))
            .process(requestHandler);
    }

    @Override
    public One<Void> addTimeToGame(String gameId, int seconds) {
        return Endpoint.addTimeToGame.newRequest(request -> request
                .path(gameId, seconds))
            .process(requestHandler);
    }

    @Override
    public One<BulkPairing> bulk(String bulkId) {
        return Endpoint.bulkPairingGet.newRequest(request -> request
                .path(bulkId))
            .process(requestHandler);
    }

    @Override
    public Many<BulkPairing> bulks() {
        return Endpoint.bulkPairingGetMany.newRequest(request -> {})
            .process(requestHandler);
    }

    @Override
    public One<BulkPairing> createBulk(Consumer<BulkBuilder> consumer) {
        return Endpoint.bulkPairingCreate.newRequest(request -> request
                .body(bulkBuilderToMap(consumer)))
            .process(requestHandler);
    }

    @Override
    public One<Void> startBulk(String bulkId) {
        return Endpoint.bulkPairingStart.newRequest(request -> request
                .path(bulkId))
            .process(requestHandler);
    }

    @Override
    public One<Void> cancelBulk(String bulkId) {
        return Endpoint.bulkPairingCancel.newRequest(request -> request
                .path(bulkId))
            .process(requestHandler);
    }

    private Map<String, Object> bulkBuilderToMap(Consumer<BulkBuilder> consumer) {
        List<BulkParams.Pairing> pairings = new ArrayList<>();
        Set<String> rules = new HashSet<>();
        var builder = MapBuilder.of(BulkParams.class)
            .addCustomHandler("addPairing", (args, map) -> pairings.add(BulkParams.Pairing.class.cast(args[0])))
            .addCustomHandler("noAbort",    (args, map) -> rules.add("noAbort"))
            .addCustomHandler("noRematch",  (args, map) -> rules.add("noRematch"))
            .addCustomHandler("noGiveTime", (args, map) -> rules.add("noGiveTime"))
            .addCustomHandler("noClaimWin", (args, map) -> rules.add("noClaimWin"))
            .addCustomHandler("noEarlyDraw", (args, map) -> rules.add("noEarlyDraw"))
            ;
        var bulkBuilder = new BulkBuilder() {
            @Override
            public BulkParams clock(int initial, int increment) {
                return builder
                    .add("clock.limit", initial)
                    .add("clock.increment", increment)
                    .proxy();
            }

            @Override
            public BulkParams daysPerTurn(int daysPerTurn) {
                return builder
                    .add("days", daysPerTurn)
                    .proxy();
            }
        };

        record Pairings(List<BulkParams.Pairing> pairings) {
            @Override
            public String toString() {
                return pairings.stream()
                    .map(p -> String.valueOf(p.tokenWhite().get()) + ":" + String.valueOf(p.tokenBlack().get()))
                    .collect(Collectors.joining(","));
            }
        }

        consumer.accept(bulkBuilder);
        var map = builder.toMap();
        map.putIfAbsent("rated", false);
        map.put("players", new Pairings(pairings));
        if (!rules.isEmpty()) map.put("rules", String.join(",", rules));
        return map;
    }
}
