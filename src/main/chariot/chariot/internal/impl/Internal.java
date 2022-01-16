package chariot.internal.impl;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.time.ZonedDateTime;

import chariot.internal.Crypt;
import chariot.api.Games.*;
import chariot.model.*;
import chariot.model.Broadcast.Round;
import chariot.model.ChallengeResult.*;
import chariot.model.Enums.*;

public interface Internal {

    class MapBuilder {
        final Map<String, Object> map = new HashMap<>();
        final Map<String, InvocationHandler> customHandlers = new HashMap<>();

        final InvocationHandler basicHandler = (proxy, method, args) -> {
            map.put(method.getName(), args[0]);
            return proxy;
        };

        void addCustomHandler(String methodName, BiConsumer<Object[], Map<String,Object>> argsConsumer) {
            customHandlers.put(methodName, (proxy, method, args) -> {;
                argsConsumer.accept(args, map);
                return proxy;
            });
        }

        Map<String, Object> getMap() { return map; }

        <T> T of(Class<T> interfaceClazz) {

            Object proxyInstance = Proxy.newProxyInstance(
                    interfaceClazz.getClassLoader(),
                    new Class<?>[] { interfaceClazz },
                    (proxy, method, args) -> {

                        if (method.isDefault()) {

                            Class<?> declaringClass = method.getDeclaringClass();
                            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup());
                            MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
                            MethodHandle handle = Modifier.isStatic(method.getModifiers()) ?
                                lookup.findStatic(declaringClass, method.getName(), methodType) :
                                lookup.findSpecial(declaringClass, method.getName(), methodType, declaringClass);

                            Object result = handle.bindTo(proxy).invokeWithArguments(args);
                            return result;
                        }

                        var handler = customHandlers.getOrDefault(method.getName(), basicHandler);
                        try {
                            var result = handler.invoke(proxy, method, args);
                            return result;
                        } catch (InvocationTargetException ex) {
                            throw ex.getCause();
                        }
                    });

            @SuppressWarnings("unchecked")
            T t = (T) proxyInstance;

            return t;
        }

    }

    interface ConsumerParams<U, T> {
        default U of(Class<T> clazz, Consumer<T> consumer) {
            var builder = new MapBuilder();
            addOverrides(builder);
            var proxy = builder.of(clazz);
            consumer.accept(proxy);
            return toParams(builder.getMap());
        }

        default MapBuilder addOverrides(MapBuilder builder) { return builder; }

        U toParams(Map<String, Object> map);
    }

    class Base<U,T> implements ConsumerParams<U,T> {
        Map<String, Object> map = new HashMap<>();
        public Map<String, Object> toMap() { return map; }

        public U toParams(Map<String, Object> map) {
            this.map = map;
            @SuppressWarnings("unchecked")
            U u = (U) this;
            return u;
        }
    }

    interface Games extends chariot.api.Games {
        Result<Game> byGameId(String gameId, InternalGameParams params);
        Result<Game> byUserId(String userId, InternalSearchFilter params);
        Result<Game> currentByUserId(String userId, InternalGameParams params);
        Result<Game> byChannel(Channel channel, ChannelParameters params);
        Result<Game> byGameIds(Set<String> gameIds, InternalGameParams params);
        Result<ExploreResult> openingExplorerMasters(MastersParameters params);
        Result<ExploreResult> openingExplorerLichess(LichessParameters params);
        Result<ExploreResult> openingExplorerPlayer(PlayerParameters params);
        default Result<StreamGame> streamGamesByUserIds(Set<String> userIds) { return streamGamesByUserIds(true, userIds); }
        default Result<Game> byGameId(String gameId, Consumer<GameParams> params) { return byGameId(gameId, new InternalGameParams().of(GameParams.class, params)); }
        default Result<Game> byUserId(String userId, Consumer<SearchFilter> params) { return byUserId(userId, new InternalSearchFilter().of(SearchFilter.class, params)); }
        default Result<Game> currentByUserId(String userId, Consumer<GameParams> params) { return currentByUserId(userId, new InternalGameParams().of(GameParams.class, params)); }
        default Result<Game> byChannel(Channel channel, Consumer<ChannelFilter> params) { return byChannel(channel, new ChannelParameters().of(ChannelFilter.class, params)); }
        default Result<Game> byChannel(Function<Channel.Provider, Channel> channel, Consumer<ChannelFilter> params) { return byChannel(channel.apply(Channel.provider()), new ChannelParameters().of(ChannelFilter.class, params)); }
        default Result<Game> byGameIds(Set<String> gameIds, Consumer<GameParams> params) { return byGameIds(gameIds, new InternalGameParams().of(GameParams.class, params)); }
        default Result<ExploreResult> openingExplorerMasters(Consumer<MastersBuilder> params) { return openingExplorerMasters(new MastersParameters().of(MastersBuilder.class, params)); }
        default Result<ExploreResult> openingExplorerLichess(Consumer<LichessBuilder> params) { return openingExplorerLichess(new LichessParameters().of(LichessBuilder.class, params)); }
        default Result<ExploreResult> openingExplorerPlayer(String userId, Consumer<PlayerBuilder> params) { return openingExplorerPlayer(new PlayerParameters(userId).of(PlayerBuilder.class, params)); }

        class InternalGameParams extends Base<InternalGameParams, GameParams> {}
        class ChannelParameters extends Base<ChannelParameters, ChannelFilter> {}
        class MastersParameters extends Base<MastersParameters, MastersBuilder> {}

        class InternalSearchFilter extends Base<InternalSearchFilter, SearchFilter> {

            @Override
            public MapBuilder addOverrides(MapBuilder builder) {
                builder.addCustomHandler("perfType", (args, map) -> {
                    PerfType[] perfTypes = (PerfType[]) args[0];
                    map.put("perfType",
                            Arrays.stream(perfTypes)
                            .map(PerfType::name)
                            .collect(Collectors.joining(","))
                           );
                });
                builder.addCustomHandler("sortAscending", (args, map) -> {
                    boolean ascending = (boolean) args[0];
                    map.put("sort", ascending ? "dateAsc" : "dateDesc");
                });
                return builder;
            }
         }

        class LichessParameters extends Base<LichessParameters, LichessBuilder> {

            @Override
            public MapBuilder addOverrides(MapBuilder builder) {
                builder.addCustomHandler("speeds", (args, map) -> {
                    @SuppressWarnings("unchecked")
                    var speeds = (Set<Speed>) args[0];
                    if (! speeds.isEmpty()) {
                        map.put("speeds", speeds.stream().map(Speed::name).collect(Collectors.joining(",")));
                    }
                });
                builder.addCustomHandler("ratings", (args, map) -> {
                    @SuppressWarnings("unchecked")
                    var ratings = (Set<LichessBuilder.RatingGroup>) args[0];
                    if (! ratings.isEmpty()) {
                        map.put("ratings", ratings.stream().map(LichessBuilder.RatingGroup::asString).collect(Collectors.joining(",")));
                    }
                });
                return builder;
            }
        }

        class PlayerParameters extends Base<PlayerParameters, PlayerBuilder> {

            final String userId;

            PlayerParameters(String userId) {
                this.userId = userId;
            }

            @Override
            public MapBuilder addOverrides(MapBuilder builder) {
                var m = builder.getMap();
                m.put("player", userId);
                m.put("color", Color.white);

                builder.addCustomHandler("speeds", (args, map) -> {
                    @SuppressWarnings("unchecked")
                    var speeds = (Set<Speed>) args[0];
                    if (! speeds.isEmpty()) {
                        map.put("speeds", speeds.stream().map(Speed::name).collect(Collectors.joining(",")));
                    }
                });
                builder.addCustomHandler("ratings", (args, map) -> {
                    @SuppressWarnings("unchecked")
                    var modes = (Set<Games.PlayerBuilder.Mode>) args[0];
                    if (! modes.isEmpty()) {
                        map.put("modes", modes.stream().map(Games.PlayerBuilder.Mode::name).collect(Collectors.joining(",")));
                    }
                });
                return builder;
            }
        }
    }

    interface GamesAuth extends chariot.api.GamesAuth {
        Result<NowPlaying> ongoingGames(Optional<Integer> nb);
        default Result<NowPlaying> ongoingGames() { return ongoingGames(Optional.empty()); }
        default Result<NowPlaying> ongoingGames(int nb) { return ongoingGames(Optional.of(nb)); }
    }
    interface Tournaments extends chariot.api.Tournaments {
        Result<Game> gamesByArenaId(String arenaId, TournamentParams params);
        Result<Game> gamesBySwissId(String swissId, TournamentParams params);
        Result<Arena> arenaById(String arenaId, Optional<Integer> page);
        default Result<Arena> arenaById(String arenaId) { return arenaById(arenaId, Optional.empty()); }
        default Result<Arena> arenaById(String arenaId, int page) { return arenaById(arenaId, Optional.of(page)); }
        Result<ArenaResult> resultsByArenaId(String arenaId, Optional<Integer> nb);
        Result<SwissResult> resultsBySwissId(String swissId, Optional<Integer> nb);
        default Result<Game> gamesByArenaId(String arenaId) { return gamesByArenaId(arenaId, __ -> {}); }
        default Result<Game> gamesBySwissId(String swissId) { return gamesBySwissId(swissId, __ -> {}); }
        default Result<Game> gamesByArenaId(String arenaId, Consumer<Filter> params) { return gamesByArenaId(arenaId, new TournamentParams().of(Filter.class, params)); }
        default Result<Game> gamesBySwissId(String swissId, Consumer<Filter> params) { return gamesBySwissId(swissId, new TournamentParams().of(Filter.class, params)); }
        default Result<ArenaResult> resultsByArenaId(String arenaId) { return resultsByArenaId(arenaId, Optional.empty()); }
        default Result<ArenaResult> resultsByArenaId(String arenaId, int nb) { return resultsByArenaId(arenaId, Optional.of(nb)); }
        default Result<SwissResult> resultsBySwissId(String swissId) { return resultsBySwissId(swissId, Optional.empty()); }
        default Result<SwissResult> resultsBySwissId(String swissId, int nb) { return resultsBySwissId(swissId, Optional.of(nb)); }
        default Result<Tournament> arenasCreatedByUserId(String userId) { return arenasCreatedByUserId(userId, Set.of()); }

        class TournamentParams extends Base<TournamentParams, Filter> {}
    }

    interface TournamentsAuth extends chariot.api.TournamentsAuth, Tournaments {
        Result<Arena> createArena(ArenaParameters params);
        Result<Arena> updateArena(String id, ArenaParameters params);
        Result<Swiss> createSwiss(String teamId, SwissParameters params);
        Result<Swiss> updateSwiss(String id, SwissParameters params);
        Result<Ack> joinArena(String id, Optional<String> password, Optional<String> teamId);
        Result<Ack> joinSwiss(String id, Optional<String> password);
        default Result<Ack> joinSwiss(String id) { return joinSwiss(id, Optional.empty()); }
        default Result<Ack> joinSwiss(String id, String password) { return joinSwiss(id, Optional.of(password)); }
        default Result<Ack> joinArena(String id) { return joinArena(id, Optional.empty(), Optional.empty()); }
        default Result<Ack> joinArena(String id, String password) { return joinArena(id, Optional.of(password), Optional.empty()); }
        default Result<Ack> joinArenaForTeam(String id, String team) { return joinArena(id, Optional.empty(), Optional.of(team)); }
        default Result<Ack> joinArenaForTeam(String id, String team, String password) { return joinArena(id, Optional.of(password), Optional.of(team)); }
        default Result<Arena> createArena(Function<chariot.api.TournamentsAuth.ArenaBBuilder, chariot.api.TournamentsAuth.ArenaBuilder> params) { return createArena(ArenaParameters.of(params)); }
        default Result<Arena> updateArena(String id, Function<chariot.api.TournamentsAuth.ArenaBBuilder, chariot.api.TournamentsAuth.ArenaBuilder> params) { return updateArena(id, ArenaParameters.of(params)); }
        default Result<Swiss> createSwiss(String teamId, Function<chariot.api.TournamentsAuth.SwissBBuilder, chariot.api.TournamentsAuth.SwissBuilder> params) { return createSwiss(teamId, SwissParameters.of(params)); }
        default Result<Swiss> updateSwiss(String id, Function<chariot.api.TournamentsAuth.SwissBBuilder, chariot.api.TournamentsAuth.SwissBuilder> params) { return updateSwiss(id, SwissParameters.of(params)); }
        default Result<Arena> updateTeamBattle(String id, int nbLeaders, String... teamIds) { return updateTeamBattle(id, nbLeaders, Set.of(teamIds)); }

        sealed interface ArenaParameters {
            record Parameters(Map<String,Object> params) implements ArenaParameters { }
            public static ArenaParameters of(Function<ArenaBBuilder, ArenaBuilder> function) {

                var arenabbuilder = new ArenaBBuilder() {
                    MapBuilder builder = new MapBuilder();
                    public ArenaBuilder clock(ClockInitial clockInitial, int clockIncrement, int arenaMinutes) {

                        Objects.requireNonNull(clockInitial);
                        if (clockIncrement < 0 || clockIncrement > 180) {
                            throw new IllegalArgumentException("clock.increment [" + clockIncrement + "] not allowed. Must be [ 0 .. 180 ]");
                        }
                        if (clockInitial == ClockInitial._0 && clockIncrement == 0) {
                            throw new IllegalArgumentException("clock.initial and clock.increment can't both be 0");
                        }
                        if (arenaMinutes < 0 || arenaMinutes > 360) throw new RuntimeException("minutes [%d] must be between [ 0 .. 360 ]".formatted(arenaMinutes));

                        builder.addCustomHandler("startTime", (args, map) -> {
                            @SuppressWarnings("unchecked")
                            var startTime = (Function<ArenaBuilder.StartTime.Provider, ArenaBuilder.StartTime>) args[0];

                            var time = startTime.apply(ArenaBuilder.StartTime.provider());
                            if (time instanceof ArenaBuilder.StartTime.InMinutes m) {
                                map.put("waitMinutes", m.waitMinutes());
                            } else if (time instanceof ArenaBuilder.StartTime.AtDate d) {
                                map.put("startDate", d.startDate());
                            }
                        });
                        builder.addCustomHandler("conditionTeam", (args, map) -> { map.put("conditions.teamMember.teamId", args[0]); });
                        builder.addCustomHandler("conditionMinRating", (args, map) -> { map.put("conditions.minRating.rating", args[0]); });
                        builder.addCustomHandler("conditionMaxRating", (args, map) -> { map.put("conditions.maxRating.rating", args[0]); });
                        builder.addCustomHandler("conditionMinRatedGames", (args, map) -> { map.put("conditions.nbRatedGames.nb", args[0]); });

                        var map = builder.getMap();
                        var proxy = builder.of(ArenaBuilder.class);

                        map.put("clockTime", clockInitial.minutes);
                        map.put("clockIncrement", clockIncrement);
                        map.put("minutes", arenaMinutes);

                        return proxy;

                    }
                };

                @SuppressWarnings("unused")
                var builder = function.apply(arenabbuilder);

                return new Parameters(arenabbuilder.builder.getMap());
            }
        }

        sealed interface SwissParameters {
            record Parameters(Map<String,Object> params) implements SwissParameters {}
            default public Map<String, Object> toMap() {
                if (this instanceof Parameters p)
                    return p.params();
                else
                    return Map.of();
            }
            public static SwissParameters of(Function<SwissBBuilder, SwissBuilder> function) {
                var swissbbuilder = new SwissBBuilder() {
                    MapBuilder builder = new MapBuilder();

                    public SwissBuilder clock(int clockInitial, int clockIncrement) {
                        if (clockInitial < 0 || clockInitial > 3600)
                            throw new IllegalArgumentException("clockInitial [" + clockInitial + "] not allowed. Must be [ 0 .. 3600 ]");
                        if (clockIncrement < 0 || clockIncrement > 600)
                            throw new IllegalArgumentException("clockIncrement [" + clockIncrement + "] not allowed. Must be [ 0 .. 600 ]");
                        if (clockInitial == 0 && clockIncrement == 0)
                            throw new IllegalArgumentException("clockInitial and clockIncrement can't both be 0");

                        builder.addCustomHandler("nbRounds", (args, map) -> {
                            int nbRounds = (int) args[0];
                            if (nbRounds < 3 || nbRounds > 100)
                                throw new IllegalArgumentException("nbRounds [" + nbRounds + "] not allowed. Must be [ 3 .. 100 ]");
                            map.put("nbRounds", nbRounds);
                        });
                        builder.addCustomHandler("roundInterval", (args, map) -> {
                            int roundInterval = (int) args[0];
                            if (roundInterval < 0 || roundInterval > 86400)
                                if (roundInterval != 99999999)
                                    throw new IllegalArgumentException("roundInterval [" + roundInterval + "] not allowed. Must be [ 0 .. 86400 ] or 99999999");
                            map.put("roundInterval", roundInterval);
                        });
                        builder.addCustomHandler("chatFor", (args, map) -> {
                            var chatFor = (ChatFor) args[0];
                            Objects.requireNonNull(chatFor);
                            map.put("chatFor", chatFor.id);
                        });

                        var map = builder.getMap();
                        map.put("nbRounds", 9);
                        map.put("clock.limit", clockInitial);
                        map.put("clock.increment", clockIncrement);

                        var swissBuilder = builder.of(SwissBuilder.class);
                        return swissBuilder;
                    }
                };

                @SuppressWarnings("unused")
                var builder = function.apply(swissbbuilder);

                return new Parameters(swissbbuilder.builder.getMap());
            }
        }
    }

    interface ChallengesInternal extends chariot.api.Challenges {

        Result<ChallengeOpenEnded> challengeOpenEnded(ChallengeOpenEndedParameters parameters);

        default Result<ChallengeOpenEnded> challengeOpenEnded(Function<OpenEndedBBuilder, OpenEndedBuilder> params) {
            return challengeOpenEnded(ChallengeOpenEndedParameters.of(params));
        }


        sealed interface ChallengeOpenEndedParameters {
            record Parameters(Map<String, Object> params) implements ChallengeOpenEndedParameters { }
            record Empty() implements ChallengeOpenEndedParameters {}

            static ChallengeOpenEndedParameters empty() { return new Empty(); }

            default public Map<String, Object> toMap() {
                if (this instanceof Parameters p)
                    return p.params();
                else
                    return Map.of();
            }

            public static ChallengeOpenEndedParameters of(Function<OpenEndedBBuilder, OpenEndedBuilder> params) {
                var builder = params.apply(BBuilder.builder());
                if (builder instanceof Builder b) return b.build(); else return new Parameters(Map.of());
            }

            public class BBuilder implements OpenEndedBBuilder {
                private BBuilder() {}
                private static BBuilder builder() { return new BBuilder(); }
                public Builder clock(int clockInitial, int clockIncrement) { return new Builder(clockInitial, clockIncrement); }
            }

            public class Builder implements OpenEndedBuilder{
                private Map<String, Object> map = new HashMap<>();
                private Builder(int clockInitial, int clockIncrement) {
                    if (clockInitial < 0 || clockInitial > 10800) {
                        throw new IllegalArgumentException("clock.initial [" + clockInitial + "] not allowed. Must be [ 0 .. 10800 ]");
                    }
                    if (clockIncrement < 0 || clockIncrement > 60) {
                        throw new IllegalArgumentException("clock.increment [" + clockIncrement + "] not allowed. Must be [ 0 .. 60 ]");
                    }
                    if (clockInitial == 0 && clockIncrement == 0) {
                        throw new IllegalArgumentException("clock.initial and clock.increment can't both be 0");
                    }
                    map.put("clock.limit", clockInitial);
                    map.put("clock.increment", clockIncrement);
                }
                private ChallengeOpenEndedParameters build() { return new Parameters(map); }
                public Builder rated(boolean rated) { map.put("rated", rated); return this; }
                public Builder variant(VariantName variant) { Objects.requireNonNull(variant); map.put("variant", variant); return this; }
                public Builder variant(Function<VariantName.Provider, VariantName> variant) { return variant(variant.apply(VariantName.provider())); }
                public Builder fen(String fen) { Objects.requireNonNull(fen); map.put("fen", fen); return this; }
                public Builder name(String name) { Objects.requireNonNull(name); map.put("name", name); return this; }
            }
        }
    }

    interface ChallengeAuth extends chariot.api.ChallengesAuth, ChallengeAuthCommon {
        Result<BulkPairing> createBulk(InternalBulkParameters params);

        default Result<BulkPairing> createBulk(Function<BulkBBuilder, BulkBuilder> params) {
            return createBulk(InternalBulkParameters.of(params));
        }

        sealed interface InternalBulkParameters {
            record Parameters(Map<String,Object> params) implements InternalBulkParameters { }
            record Empty() implements InternalBulkParameters {}
            default public Map<String, Object> toMap() {
                if (this instanceof Parameters p)
                    return p.params();
                else
                    return Map.of();
            }
            public static InternalBulkParameters empty() { return new Empty(); }
            public static InternalBulkParameters of(Function<BulkBBuilder, BulkBuilder> params) {
                var builder = params.apply(BBuilder.builder());
                if (builder instanceof Builder b) return b.build(); else return new Parameters(Map.of());
            }
            public class BBuilder implements BulkBBuilder {
                private BBuilder() {}
                private static BBuilder builder() { return new BBuilder(); }
                public Builder clock(int clockInitial, int clockIncrement) { return new Builder(false, clockInitial, clockIncrement); }
            }
            record Pairings(List<BulkBuilder.Pairing> pairings) {
                @Override
                public String toString() {
                    return pairings.stream()
                        .map(p -> String.valueOf(p.tokenWhite().get()) + ":" + String.valueOf(p.tokenBlack().get()))
                        .collect(Collectors.joining(","));
                }
            }

            public class Builder implements BulkBuilder {
                private Map<String, Object> map = new HashMap<>();
                private List<Pairing> pairings = new ArrayList<>();
                private Builder(boolean rated, int clockInitial, int clockIncrement) {
                    if (clockInitial < 0 || clockInitial > 10800)
                        throw new IllegalArgumentException("clock.initial [" + clockInitial + "] not allowed. Must be [ 0 .. 10800 ]");
                    if (clockIncrement < 0 || clockIncrement > 60)
                        throw new IllegalArgumentException("clock.increment [" + clockIncrement + "] not allowed. Must be [ 0 .. 60 ]");
                    if (clockInitial == 0 && clockIncrement == 0)
                        throw new IllegalArgumentException("clock.initial and clock.increment can't both be 0");
                    map.put("rated", rated);
                    map.put("clock.limit", clockInitial);
                    map.put("clock.increment", clockIncrement);
                }
                public InternalBulkParameters build() {
                    // Validation...
                    if (pairings.isEmpty())
                        throw new RuntimeException("You must add pairings in order to create pairings!");

                    map.put("players", new Pairings(pairings));
                    return new Parameters(map);
                }
                public Builder addPairing(String tokenWhite, String tokenBlack) {
                    // User is supplying plain text tokens.
                    // Let's make an effort, albeit small, and obfuscate the tokens
                    // so we don't keep them in memory in plain text.
                    var encWhite = Crypt.encrypt(tokenWhite.toCharArray());
                    var encBlack = Crypt.encrypt(tokenBlack.toCharArray());

                    addPairing(new Pairing(
                                () -> Crypt.decrypt(encWhite.data(), encWhite.key()),
                                () -> Crypt.decrypt(encBlack.data(), encBlack.key()))
                            );
                    return this;
                }
                public Builder addPairing(Supplier<char[]> tokenWhite, Supplier<char[]> tokenBlack) { addPairing(new Pairing(tokenWhite, tokenBlack)); return this; }
                public Builder rated(boolean rated) { map.put("rated", rated); return this; }
                public Builder addPairing(Pairing pairing) { pairings.add(pairing); return this; }
                public Builder pairAt(ZonedDateTime pairAt) { map.put("pairAt", pairAt); return this; }
                public Builder startClocksAt(ZonedDateTime startClocksAt) { map.put("startClocksAt", startClocksAt); return this; }
                public Builder variant(VariantName variant) { map.put("variant", variant); return this; }
                public Builder variant(Function<VariantName.Provider, VariantName> variant) { return variant(variant.apply(VariantName.provider())); }
                public Builder message(String message) { map.put("message", message); return this; }
            }
        }

    }

    interface ChallengeAuthCommon extends chariot.api.ChallengesAuthCommon {

        Result<Challenge>   challenge(String userId, InternalChallengeParameters params);
        Result<ChallengeAI> challengeAI(InternalChallengeAIParameters params);
        Result<Ack>         cancelChallenge(String challengeId, Optional<Supplier<char[]>> opponentToken);
        Result<Ack>         declineChallenge(String challengeId, Optional<DeclineReason> reason);


        // "Implementation" of ChallengesAuthCommon
        default Result<Challenge> challenge(String userId, Function<ChallengeBBuilder, ChallengeBuilder> params) {
            return challenge(userId, InternalChallengeParameters.of(params));
        }

        default Result<ChallengeAI> challengeAI(Function<ChallengeAIBBuilder, ChallengeAIBuilder> params) {
            return challengeAI(InternalChallengeAIParameters.of(params));
        }

        default Result<Ack> cancelChallenge(String challengeId) {
            return cancelChallenge(challengeId, Optional.empty());
        }

        default Result<Ack> cancelChallenge(String challengeId, Supplier<char[]> opponentToken) {
            return cancelChallenge(challengeId, Optional.of(opponentToken));
        }

        default Result<Ack> declineChallenge(String challengeId) {
            return declineChallenge(challengeId, Optional.empty());
        }

        default Result<Ack> declineChallenge(String challengeId, DeclineReason reason) {
            return declineChallenge(challengeId, Optional.of(reason));
        }

        sealed interface InternalChallengeParameters {

            record Parameters(Map<String, Object> params) implements InternalChallengeParameters { }
            record Empty() implements InternalChallengeParameters {}

            static InternalChallengeParameters empty() { return new Empty(); }

            default public Map<String, Object> toMap() {
                if (this instanceof Parameters p)
                    return p.params();
                else
                    return Map.of();
            }

            public static InternalChallengeParameters of(Function<ChallengeBBuilder, ChallengeBuilder> params) {
                var builder = params.apply(BBuilder.builder());
                if (builder instanceof Builder b) return b.build(); else return new Parameters(Map.of());
            }

            public class BBuilder implements ChallengeBBuilder {
                private BBuilder() {}
                private static BBuilder builder() { return new BBuilder(); }
                public Builder clock(DaysPerTurn d) { return new Builder(d); }
                public Builder clock(int clockInitial, int clockIncrement) { return new Builder(clockInitial, clockIncrement); }
            }

            public class Builder implements ChallengeBuilder {

                private Map<String, Object> map = new HashMap<>();

                private Builder(DaysPerTurn daysPerTurn) {
                    Objects.requireNonNull(daysPerTurn);
                    map.put("days", daysPerTurn.days);
                }

                private Builder(int clockInitial, int clockIncrement) {
                    if (clockInitial < 0 || clockInitial > 10800) {
                        throw new IllegalArgumentException("clock.initial [" + clockInitial + "] not allowed. Must be [ 0 .. 10800 ]");
                    }
                    if (clockIncrement < 0 || clockIncrement > 60) {
                        throw new IllegalArgumentException("clock.increment [" + clockIncrement + "] not allowed. Must be [ 0 .. 60 ]");
                    }
                    if (clockInitial == 0 && clockIncrement == 0) {
                        throw new IllegalArgumentException("clock.initial and clock.increment can't both be 0");
                    }
                    map.put("clock.limit", clockInitial);
                    map.put("clock.increment", clockIncrement);
                }
                private InternalChallengeParameters build() { return new Parameters(map); }
                public Builder rated(boolean rated) { map.put("rated", rated); return this; }
                public Builder color(ColorPref color) { Objects.requireNonNull(color); map.put("color", color); return this; }
                public Builder variant(VariantName variant) { Objects.requireNonNull(variant); map.put("variant", variant); return this; }
                public Builder fen(String fen) { Objects.requireNonNull(fen); map.put("fen", fen); return this; }
                public Builder keepAliveStream(boolean keepAliveStream) { map.put("keepAliveStream", keepAliveStream); return this; }
                public Builder acceptByToken(String acceptByToken) { Objects.requireNonNull(acceptByToken); map.put("acceptByToken", acceptByToken); return this; }
                public Builder acceptByToken(String acceptByToken, String message) {
                    Objects.requireNonNull(acceptByToken);
                    Objects.requireNonNull(message);
                    if (! message.contains("{game}")) {
                        throw new IllegalArgumentException("message must conntain {game}");
                    }
                    map.put("acceptByToken", acceptByToken);
                    map.put("message", message);
                    return this;
                }
            }
        }

        sealed interface InternalChallengeAIParameters {
            record Parameters(Map<String, Object> params) implements InternalChallengeAIParameters { }
            record Empty() implements InternalChallengeAIParameters {}
            static InternalChallengeAIParameters empty() { return new Empty(); }
            default public Map<String, Object> toMap() {
                if (this instanceof Parameters p)
                    return p.params();
                else
                    return Map.of();
            }
            public static InternalChallengeAIParameters of(Function<ChallengeAIBBuilder, ChallengeAIBuilder> params) {
                var builder = params.apply(BBuilder.builder());
                if (builder instanceof Builder b) return b.build(); else return new Parameters(Map.of());
            }
            public class BBuilder implements ChallengeAIBBuilder {
                private BBuilder() {}
                private static BBuilder builder() { return new BBuilder(); }
                public Builder clock(DaysPerTurn d) { return new Builder(Level._1, d); }
                public Builder clock(int clockInitial, int clockIncrement) { return new Builder(Level._1, clockInitial, clockIncrement); }
            }

            public class Builder implements ChallengeAIBuilder {
                private Map<String, Object> map = new HashMap<>();
                private Builder(Level level, DaysPerTurn daysPerTurn) {
                    Objects.requireNonNull(level);
                    Objects.requireNonNull(daysPerTurn);
                    map.put("level", level.level);
                    map.put("days", daysPerTurn.days);
                }
                private Builder(Level level, int clockInitial, int clockIncrement) {
                    Objects.requireNonNull(level);
                    if (clockInitial < 0 || clockInitial > 10800) {
                        throw new IllegalArgumentException("clock.initial [" + clockInitial + "] not allowed. Must be [ 0 .. 10800 ]");
                    }
                    if (clockIncrement < 0 || clockIncrement > 60) {
                        throw new IllegalArgumentException("clock.increment [" + clockIncrement + "] not allowed. Must be [ 0 .. 60 ]");
                    }
                    if (clockInitial == 0 && clockIncrement == 0) {
                        throw new IllegalArgumentException("clock.initial and clock.increment can't both be 0");
                    }
                    map.put("level", level.level);
                    map.put("clock.limit", clockInitial);
                    map.put("clock.increment", clockIncrement);
                }
                private InternalChallengeAIParameters build() { return new Parameters(map); }
                public Builder level(Level level) { Objects.requireNonNull(level); map.put("level", level.level); return this; }
                public Builder color(ColorPref color) { Objects.requireNonNull(color); map.put("color", color); return this; }
                public Builder variant(VariantName variant) { Objects.requireNonNull(variant); map.put("variant", variant); return this; }
                public Builder fen(String fen) { Objects.requireNonNull(fen); map.put("fen", fen); return this; }
            }
        }
    }

    interface BotAuth extends chariot.api.BotAuth {

        // chariot.api.Bot, but shares impl with BotAuthImpl
        Result<User> botsOnline(Optional<Integer> nb);
        default Result<User> botsOnline() {
            return botsOnline(Optional.empty());
        }
        default Result<User> botsOnline(int nb) {
            return botsOnline(Optional.of(nb));
        }

        Result<Ack> move(String gameId, String move, Optional<Boolean> drawOffer);
        default Result<Ack> move(String gameId, String move) {
            return move(gameId, move, Optional.empty());
        }
        default Result<Ack> move(String gameId, String move, boolean drawOffer) {
            return move(gameId, move, Optional.of(drawOffer));
        }
    }

    interface BoardAuth extends chariot.api.BoardAuth, ChallengeAuthCommon {
        Result<Ack> seek(SeekParameters params);
        Result<Ack> move(String gameId, String move, Optional<Boolean> drawOffer);

        default Result<Ack> move(String gameId, String move) {
            return move(gameId, move, Optional.empty());
        }

        default Result<Ack> move(String gameId, String move, boolean drawOffer) {
            return move(gameId, move, Optional.of(drawOffer));
        }


        default Result<Ack> seek(Function<SeekBBuilder, SeekBuilder> params) {
            return seek(SeekParameters.of(params));
        }

        sealed interface SeekParameters {
            public record Parameters(Map<String, Object> params) implements SeekParameters { }
            default public Map<String, Object> toMap() {
                if (this instanceof Parameters p)
                    return p.params();
                else
                    return Map.of();
            }

            public static SeekParameters of(Function<SeekBBuilder, SeekBuilder> params) {
                var builder = params.apply(BBuilder.builder());
                if (builder instanceof Builder b) return b.build(); else return new Parameters(Map.of());
            }

            public class BBuilder implements SeekBBuilder {
                private BBuilder() {}
                private static BBuilder builder() { return new BBuilder(); }
                public Builder clock(DaysPerTurn d) { return new Builder(d); }
                public Builder clock(int clockInitial, int clockIncrement) { return new Builder(clockInitial, clockIncrement); }
            }

            public class Builder implements SeekBuilder {
                private Map<String, Object> map = new HashMap<>();
                private Builder(DaysPerTurn daysPerTurn) {
                    Objects.requireNonNull(daysPerTurn);
                    map.put("days", daysPerTurn.days);
                }
                private Builder(int clockInitial, int clockIncrement) {
                    if (clockInitial < 0 || clockInitial > 180) {
                        throw new IllegalArgumentException("clock.initial [" + clockInitial + "] not allowed. Must be [ 0 .. 180 ]");
                    }
                    if (clockIncrement < 0 || clockIncrement > 180) {
                        throw new IllegalArgumentException("clock.increment [" + clockIncrement + "] not allowed. Must be [ 0 .. 180 ]");
                    }
                    if (clockInitial == 0 && clockIncrement == 0) {
                        throw new IllegalArgumentException("clock.initial and clock.increment can't both be 0");
                    }
                    map.put("time", clockInitial);
                    map.put("increment", clockIncrement);
                }
                private SeekParameters build() { return new Parameters(map); }
                public Builder color(ColorPref color) { Objects.requireNonNull(color); map.put("color", color); return this; }
                public Builder variant(VariantName variant) { Objects.requireNonNull(variant); map.put("variant", variant); return this; }
                public Builder rated(boolean rated) { map.put("rated", rated); return this; }
                public Builder ratingRange(String ratingRange) { map.put("ratingRange", ratingRange); return this; }
            }
        }
    }

    interface Teams extends chariot.api.Teams {
        Result<Tournament>   arenaByTeamId(String teamId, Optional<Integer> max);
        Result<Swiss>       swissByTeamId(String teamId, Optional<Integer> max);
        Result<Team>        search(Optional<String> text);
        Result<PageTeam>    searchByPage(Optional<Integer> page, Optional<String> text);
        Result<PageTeam>    popularTeamsByPage(Optional<Integer> page);

        default int numberOfTeams() {
            var page = searchByPage();
            return page.isPresent() ?
                page.get().nbResults() :
                0;
        }

        default Result<Team> search() { return search(Optional.empty()); }
        default Result<Team> search(String text) { return search(Optional.of(text)); }
        default Result<Tournament> arenaByTeamId(String teamId) { return arenaByTeamId(teamId, Optional.of(100)); }
        default Result<Tournament> arenaByTeamId(String teamId, int max) { return arenaByTeamId(teamId, Optional.of(max)); }
        default Result<Swiss> swissByTeamId(String teamId) { return swissByTeamId(teamId, Optional.of(100)); }
        default Result<Swiss> swissByTeamId(String teamId, int max) { return swissByTeamId(teamId, Optional.of(max)); }
        default Result<PageTeam> searchByPage() { return searchByPage(Optional.empty(), Optional.empty()); }
        default Result<PageTeam> searchByPage(String text) { return searchByPage(Optional.empty(), Optional.of(text)); }
        default Result<PageTeam> searchByPage(int page) { return searchByPage(Optional.of(page), Optional.empty()); }
        default Result<PageTeam> searchByPage(int page, String text) { return searchByPage(Optional.of(page), Optional.of(text)); }
        default Result<PageTeam> popularTeamsByPage() { return popularTeamsByPage(Optional.empty()); }
        default Result<PageTeam> popularTeamsByPage(int page) { return popularTeamsByPage(Optional.of(page)); }
    }

    interface TeamsAuth extends chariot.api.TeamsAuth {

        Result<Ack> joinTeam(String teamId, Optional<String> message, Optional<String> password);

        default Result<Ack> joinTeam(String teamId) {
            return joinTeam(teamId, Optional.empty(), Optional.empty());
        }
        default Result<Ack> joinTeam(String teamId, String message) {
            return joinTeam(teamId, Optional.of(message), Optional.empty());
        }

        default Result<Ack> joinTeamPW(String teamId, String password) {
            return joinTeam(teamId, Optional.empty(), Optional.of(password));
        }
        default Result<Ack> joinTeamPW(String teamId, String password, String message) {
            return joinTeam(teamId, Optional.of(message), Optional.of(password));
        }
    }

    interface Users extends chariot.api.Users {
        Result<Crosstable> crosstable(String userId1, String userId2, Optional<Boolean> matchup);

        default Result<Crosstable> crosstable(String userId1, String userId2) {
            return crosstable(userId1, userId2, Optional.empty());
        }
        default Result<Crosstable> crosstable(String userId1, String userId2, boolean matchup) {
            return crosstable(userId1, userId2, Optional.of(matchup));
        }
    }

    interface Puzzles extends chariot.api.Puzzles {
        Result<StormDashboard> stormDashboard(String username, Optional<Integer> days);
        default Result<StormDashboard> stormDashboard(String username)           { return stormDashboard(username, Optional.empty()); }
        default Result<StormDashboard> stormDashboard(String username, int days) { return stormDashboard(username, Optional.of(days)); }
    }

    interface PuzzlesAuth extends chariot.api.PuzzlesAuth {
        Result<PuzzleActivity>         activity(Optional<Integer> max);
        default Result<PuzzleActivity> activity(int max) { return activity(Optional.of(max)); }
        default Result<PuzzleActivity> activity() { return activity(Optional.empty()); }
    }

    interface Studies extends chariot.api.Studies {

        Result<String> exportChapterByStudyAndChapterId(String studyId, String chapterId, Map<String,Object> params);
        Result<String> exportChaptersByStudyId(String studyId, Map<String,Object> params);
        Result<String> exportStudiesByUserId(String userId, Map<String,Object> params);

        default Result<String> exportChapterByStudyAndChapterId(String studyId, String chapterId, Consumer<Params> params) {
            var builder = new Builder();
            params.accept(builder);
            return exportChapterByStudyAndChapterId(studyId, chapterId, Map.copyOf(builder.map));
        }

        default Result<String> exportChaptersByStudyId(String studyId, Consumer<Params> params) {
            var builder = new Builder();
            params.accept(builder);
            return exportChaptersByStudyId(studyId, Map.copyOf(builder.map));
        }

        default Result<String> exportStudiesByUserId(String userId, Consumer<Params> params) {
            var builder = new Builder();
            params.accept(builder);
            return exportStudiesByUserId(userId, Map.copyOf(builder.map));
        }

        class Builder implements Params {
            private Map<String, Object> map = new HashMap<>();
            @Override public Params clocks(boolean clocks) { map.put("clocks", clocks); return this; }
            @Override public Params comments(boolean comments) { map.put("comments", comments); return this; }
            @Override public Params variations(boolean variations) { map.put("variations", variations); return this; }
        }
    }

    interface Analysis extends chariot.api.Analysis {

        Result<chariot.model.Analysis> cloudEval(Map<String, Object> params);

        default Result<chariot.model.Analysis> cloudEval(String fen, Consumer<Params> params) {
            var builder = new Builder();
            params.accept(builder);
            builder.map.put("fen", fen);
            return cloudEval(Map.copyOf(builder.map));
        }

        class Builder implements Params {
            private Map<String, Object> map = new HashMap<>();
            @Override public Params multiPv(int multiPv) { map.put("multiPv", multiPv); return this; }
            @Override public Params variant(VariantName variant) { map.put("variant", variant.name()); return this; }
        }
    }

    interface Broadcasts extends chariot.api.Broadcasts {
        Result<Broadcast> official(Optional<Integer> nb);
        default Result<Broadcast> official() { return official(Optional.empty()); }
        default Result<Broadcast> official(int nb) { return official(Optional.of(nb)); }
    }

    interface BroadcastsAuth extends chariot.api.BroadcastsAuth {
        Result<Broadcast> create(InternalBroadcastParameters parameters);
        Result<Round>     createRound(String tourId, InternalRoundParameters parameters);
        Result<Ack>       update(String tourId, InternalBroadcastParameters parameters);
        Result<Ack>       updateRound(String roundId, InternalRoundParameters parameters);

        default Result<Broadcast> create(Function<BroadcastBBuilder, BroadcastBuilder> params) {
            return create(InternalBroadcastParameters.of(params));
        }
        default Result<Round> createRound(String tourId, Function<RoundBBuilder, RoundBuilder> params) {
            return createRound(tourId, InternalRoundParameters.of(params));
        }
        default Result<Ack> update(String tourId, Function<BroadcastBBuilder, BroadcastBuilder> params) {
            return update(tourId, InternalBroadcastParameters.of(params));
        }
        default Result<Ack> updateRound(String roundId, Function<RoundBBuilder, RoundBuilder> params) {
            return updateRound(roundId, InternalRoundParameters.of(params));
        }

        sealed interface InternalBroadcastParameters {
            record Parameters(Map<String, Object> params) implements InternalBroadcastParameters { }
            default public Map<String, Object> toMap() {
                if (this instanceof Parameters p)
                    return p.params();
                else
                    return Map.of();
            }
            public static InternalBroadcastParameters of(Function<BroadcastBBuilder, BroadcastBuilder> params) {
                var builder = params.apply(BBuilder.builder());
                if (builder instanceof Builder b) return b.build(); else return new Parameters(Map.of());
            }
            public class BBuilder implements BroadcastBBuilder {
                private BBuilder() {}
                private static BBuilder builder() { return new BBuilder(); }
                public Builder info(String name, String description) { return new Builder(name, description); }
            }
            public class Builder implements BroadcastBuilder {
                private Map<String, Object> map = new HashMap<>();
                public Builder(String name, String description) {
                    Objects.requireNonNull(name);
                    Objects.requireNonNull(description);
                    map.put("name", name);
                    map.put("description", description);
                }
                public InternalBroadcastParameters build() { return new Parameters(map); }
                public Builder markup(String markup) { Objects.requireNonNull(markup); map.put("markup", markup); return this; }
                public Builder official(boolean official) { map.put("official", official); return this; }
            }
        }

        sealed interface InternalRoundParameters {
            record Parameters(Map<String, Object> params) implements InternalRoundParameters {}
            default public Map<String, Object> toMap() {
                if (this instanceof Parameters p)
                    return p.params();
                else
                    return Map.of();
            }
            public static InternalRoundParameters of(Function<RoundBBuilder, RoundBuilder> params) {
                var builder = params.apply(BBuilder.builder());
                if (builder instanceof Builder b) return b.build(); else return new Parameters(Map.of());
            }

            public class BBuilder implements RoundBBuilder {
                private BBuilder() {}
                private static BBuilder builder() { return new BBuilder(); }
                public Builder info(String name) { return new Builder(name); }
            }

            public class Builder implements RoundBuilder {
                private Map<String, Object> map = new HashMap<>();
                public Builder(String name) { Objects.requireNonNull(name); map.put("name", name); }
                public InternalRoundParameters build() { return new Parameters(map); }
                public Builder syncUrl(String syncUrl) { Objects.requireNonNull(syncUrl); map.put("syncUrl", syncUrl); return this; }
                public Builder startsAt(ZonedDateTime startsAt) { Objects.requireNonNull(startsAt); map.put("startsAt", startsAt.toInstant().toEpochMilli()); return this; }
                public Builder startsAt(long startsAt) { map.put("startsAt", startsAt); return this; }
            }
        }
    }
}
