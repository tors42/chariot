package chariot.internal.impl;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import chariot.Client.Scope;
import chariot.api.*;
import chariot.model.*;
import chariot.model.Enums.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;

public class ChallengesAuthCommonImpl extends ChallengesImpl implements ChallengesApiAuthCommon {

    private final Scope scope;

    public ChallengesAuthCommonImpl(RequestHandler requestHandler, Scope scope) {
        super(requestHandler);
        this.scope = scope;
    }

    @Override
    public Many<Event> connect() {
        return connect(scope);
    }

    @Override
    public One<ChallengeInfo> show(String challengeId) {
        return Endpoint.challengeShow.newRequest(request -> request
                .path(challengeId))
            .process(requestHandler);
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
    public Ack cancelChallenge(String challengeId, Supplier<char[]> opponentToken) {
        return cancelChallenge(scope, challengeId, opponentToken);
    }

    @Override
    public Ack cancelChallenge(String challengeId) {
        return cancelChallenge(scope, challengeId);
    }

    @Override
    public Ack acceptChallenge(String challengeId) {
        return acceptChallenge(scope, challengeId, Optional.empty());
    }

    @Override
    public Ack acceptChallenge(String challengeId, Function<Enums.Color.Provider, Enums.Color> color) {
        return acceptChallenge(scope, challengeId, Optional.of(color.apply(Enums.Color.provider())));
    }

    @Override
    public Ack declineChallenge(String challengeId, DeclineReason reason) {
        return declineChallenge(scope, challengeId, reason);
    }

    @Override
    public Ack declineChallenge(String challengeId) {
        return declineChallenge(scope, challengeId);
    }


    private Many<Event> connect(Scope scope) {
        return Endpoint.streamEvents.newRequest(request -> request
                .scope(scope)
                .stream())
            .process(requestHandler);
    }

    private One<Challenge> challenge(Scope scope, String userId, Consumer<ChallengeBuilder> consumer) {
        return Endpoint.challengeCreate.newRequest(request -> request
                .scope(scope)
                .path(userId)
                .body(challengeBuilderToMap(consumer)))
            .process(requestHandler);
    }

    private Many<Challenge> challengeKeepAlive(Scope scope, String userId, Consumer<ChallengeBuilder> consumer) {
        var map = challengeBuilderToMap(consumer);
        map.put("keepAliveStream", Boolean.valueOf(true));
        Many<Challenge> result = Endpoint.challengeCreateKeepAlive.newRequest(request -> request
                .scope(scope)
                .path(userId)
                .body(map)
                .stream())
            .process(requestHandler);

        if (! (result instanceof Entries<Challenge> entries)) return result;

        final Stream<Challenge> serverStream = entries.stream();

        var spliteratorAwaitingChallengedPlayer = new Spliterator<Challenge>() {
            boolean processed = false;

            @Override
            public boolean tryAdvance(Consumer<? super Challenge> action) {
                if (processed) return false;
                processed = true;
                try {
                    // User might close the StreamSupport stream we've returned to them,
                    // upon which our listener will call close on this stream we are reading from,
                    // causing an exception. All fine.
                    List<Challenge> list = serverStream.toList();

                    if (list.isEmpty()) return false;

                    if (list.size() == 1) {
                        action.accept(list.get(0));
                        return true;
                    } else if (list.size() == 2) {
                        var challenge = list.get(0);
                        var declined = (Challenge.DeclinedChallenge) list.get(1);
                        action.accept(new Challenge.DeclinedChallenge(declined.key(), declined.reason(), challenge));
                        return true;
                    }
                } catch (Exception e) {}
                return false;
            }

            @Override public Spliterator<Challenge> trySplit() { return null; }
            @Override public long estimateSize() { return 2l; }
            @Override public int characteristics() { return ORDERED; }
        };

        Stream<Challenge> userInterruptibleStream = StreamSupport.stream(spliteratorAwaitingChallengedPlayer, false);

        // Close the original stream when user closes userStream,
        // so Lichess knows to abort the challenge
        userInterruptibleStream.onClose(() -> serverStream.close());

        return Many.entries(userInterruptibleStream);
    }

    private One<ChallengeAI> challengeAI(Scope scope, Consumer<ChallengeAIBuilder> consumer) {
        return Endpoint.challengeAI.newRequest(request -> request
                .scope(scope)
                .body(challengeAIBuilderToMap(consumer)))
            .process(requestHandler);
    }

    private Ack cancelChallenge(Scope scope, String challengeId, Supplier<char[]> opponentToken) {
        return Endpoint.challengeCancel.newRequest(request -> request
                .scope(scope)
                .path(challengeId)
                .query(Map.of("opponentToken", String.valueOf(opponentToken.get()))))
            .process(requestHandler);
    }

    private Ack cancelChallenge(Scope scope, String challengeId) {
        return Endpoint.challengeCancel.newRequest(request -> request
                .scope(scope)
                .path(challengeId))
            .process(requestHandler);
    }

    private Ack acceptChallenge(Scope scope, String challengeId, Optional<Enums.Color> color) {
        return Endpoint.challengeAccept.newRequest(request -> request
                .scope(scope)
                .query(color.map(c -> Map.of("color", (Object)c)).orElseGet(Map::of))
                .path(challengeId)
                )
            .process(requestHandler);
    }

    private Ack declineChallenge(Scope scope, String challengeId, DeclineReason reason) {
        return Endpoint.challengeDecline.newRequest(request -> request
                .body(Map.of("reason", reason.name()))
                .path(challengeId))
            .process(requestHandler);
    }

    private Ack declineChallenge(Scope scope, String challengeId) {
        return Endpoint.challengeDecline.newRequest(request -> request
                .path(challengeId))
            .process(requestHandler);
    }

    private Map<String, Object> challengeBuilderToMap(Consumer<ChallengeBuilder> consumer) {
        Set<String> rules = new HashSet<>();
        var builder = MapBuilder.of(ChallengeParams.class)
            .addCustomHandler("noAbort",     (_,_) -> rules.add("noAbort"))
            .addCustomHandler("noRematch",   (_,_) -> rules.add("noRematch"))
            .addCustomHandler("noGiveTime",  (_,_) -> rules.add("noGiveTime"))
            .addCustomHandler("noClaimWin",  (_,_) -> rules.add("noClaimWin"))
            .addCustomHandler("noEarlyDraw", (_,_) -> rules.add("noEarlyDraw"));

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
            .addCustomHandler("variant", (args, map) -> {
                switch ((Variant) args[0]) {
                    case null -> map.put("variant", "standard");
                    case Variant.Basic basic -> map.put("variant", basic);
                    case Variant.Chess960(Some(String fen)) -> {
                        map.put("variant", "fromPosition");
                        map.put("fen", fen);
                    }
                    case Variant.FromPosition(Some(String fen),_) -> {
                        map.put("variant", "fromPosition");
                        map.put("fen", fen);
                    }
                    case Variant.Chess960(Empty()) -> map.put("variant", "chess960");
                    case Variant.FromPosition(Empty(),_) -> map.put("variant", "standard");
                }
            })
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
