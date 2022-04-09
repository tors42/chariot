package chariot.internal.impl;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

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
        default Result<Arena> createArena(Consumer<chariot.api.TournamentsAuth.ArenaBuilder> params) { return createArena(ArenaParameters.of(params)); }
        default Result<Arena> updateArena(String id, Consumer<chariot.api.TournamentsAuth.ArenaBuilder> params) { return updateArena(id, ArenaParameters.of(params)); }
        default Result<Swiss> createSwiss(String teamId, Consumer<chariot.api.TournamentsAuth.SwissBuilder> params) { return createSwiss(teamId, SwissParameters.of(params)); }
        default Result<Swiss> updateSwiss(String id, Consumer<chariot.api.TournamentsAuth.SwissBuilder> params) { return updateSwiss(id, SwissParameters.of(params)); }
        default Result<Arena> updateTeamBattle(String id, int nbLeaders, String... teamIds) { return updateTeamBattle(id, nbLeaders, Set.of(teamIds)); }

        sealed interface ArenaParameters {
                record Parameters(Map<String,Object> params) implements ArenaParameters { }
                public static ArenaParameters of(Consumer<ArenaBuilder> consumer) {

                var arenaBuilder = new ArenaBuilder() {
                    MapBuilder builder = new MapBuilder();

                    @Override
                    public ArenaParams clock(float initial, int increment) {

                        builder.addCustomHandler("startTime", (args, map) -> {
                            @SuppressWarnings("unchecked")
                            var startTime = (Function<ArenaParams.StartTime.Provider, ArenaParams.StartTime>) args[0];

                            var time = startTime.apply(ArenaParams.StartTime.provider());
                            if (time instanceof ArenaParams.StartTime.InMinutes m) {
                                map.put("waitMinutes", m.waitMinutes());
                            } else if (time instanceof ArenaParams.StartTime.AtDate d) {
                                map.put("startDate", d.startDate());
                            }
                        });
                        builder.addCustomHandler("conditionTeam", (args, map) -> { map.put("conditions.teamMember.teamId", args[0]); });
                        builder.addCustomHandler("conditionMinRating", (args, map) -> { map.put("conditions.minRating.rating", args[0]); });
                        builder.addCustomHandler("conditionMaxRating", (args, map) -> { map.put("conditions.maxRating.rating", args[0]); });
                        builder.addCustomHandler("conditionMinRatedGames", (args, map) -> { map.put("conditions.nbRatedGames.nb", args[0]); });
                        builder.addCustomHandler("entryCode", (args, map) -> { map.put("password", args[0]); });

                        var map = builder.getMap();
                        var proxy = builder.of(ArenaParams.class);

                        map.put("clockTime", initial);
                        map.put("clockIncrement", increment);
                        // Default duration
                        map.put("minutes", 60 + 40);

                        return proxy;
                    }
                };

                consumer.accept(arenaBuilder);

                return new Parameters(arenaBuilder.builder.getMap());
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
            public static SwissParameters of(Consumer<SwissBuilder> consumer) {
                var swissBuilder = new SwissBuilder() {
                    MapBuilder mapBuilder = new MapBuilder();

                    @Override
                    public SwissParams clock(int initial, int increment) {

                        mapBuilder.addCustomHandler("nbRounds", (args, map) -> {
                            int nbRounds = (int) args[0];
                            if (nbRounds < 3 || nbRounds > 100)
                                throw new IllegalArgumentException("nbRounds [" + nbRounds + "] not allowed. Must be [ 3 .. 100 ]");
                            map.put("nbRounds", nbRounds);
                        });
                        mapBuilder.addCustomHandler("roundInterval", (args, map) -> {
                            int roundInterval = (int) args[0];
                            if (roundInterval < 0 || roundInterval > 86400)
                                if (roundInterval != 99999999)
                                    throw new IllegalArgumentException("roundInterval [" + roundInterval + "] not allowed. Must be [ 0 .. 86400 ] or 99999999");
                            map.put("roundInterval", roundInterval);
                        });
                        mapBuilder.addCustomHandler("chatFor", (args, map) -> {
                            var chatFor = (ChatFor) args[0];
                            Objects.requireNonNull(chatFor);
                            map.put("chatFor", chatFor.id);
                        });
                        mapBuilder.addCustomHandler("entryCode", (args, map) -> { map.put("password", args[0]); });

                        mapBuilder.addCustomHandler("forbiddenPairings", (args, map) -> {
                            @SuppressWarnings("unchecked")
                            var pairings = (Collection<SwissParams.ForbiddenPairing>) args[0];

                            String forbiddenPairings = pairings.stream()
                                .map(pairing -> String.join(" ", pairing.player1(), pairing.player2()))
                                .collect(Collectors.joining("\n"));

                            var existingPairings = (String) map.get("forbiddenPairings");

                            if (existingPairings != null) {
                                forbiddenPairings = String.join("\n", existingPairings, forbiddenPairings);
                            }
                            map.put("forbiddenPairings", forbiddenPairings);
                        });

                        var map = mapBuilder.getMap();
                        map.put("clock.limit", initial);
                        map.put("clock.increment", increment);
                        // Default rounds
                        map.put("nbRounds", 9);

                        var swissBuilder = mapBuilder.of(SwissParams.class);
                        return swissBuilder;
                    }
                };

                consumer.accept(swissBuilder);

                return new Parameters(swissBuilder.mapBuilder.getMap());
            }
        }
    }

    interface ChallengesInternal extends chariot.api.Challenges {

        Result<ChallengeOpenEnded> challengeOpenEnded(ChallengeOpenEndedParameters parameters);

        default Result<ChallengeOpenEnded> challengeOpenEnded(Consumer<OpenEndedBuilder> consumer) {
            return challengeOpenEnded(ChallengeOpenEndedParameters.of(consumer));
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

            public static ChallengeOpenEndedParameters of(Consumer<OpenEndedBuilder> consumer) {

                var bbuilder = new OpenEndedBuilder() {
                    MapBuilder builder = new MapBuilder();
                    @Override
                    public OpenEndedParams clock(int initial, int increment) {
                        var map = builder.getMap();
                        var proxy = builder.of(OpenEndedParams.class);
                        map.put("clock.limit", initial);
                        map.put("clock.increment", increment);
                        return proxy;
                    }

                    @Override
                    public OpenEndedParams daysPerTurn(int daysPerTurn) {
                        var map = builder.getMap();
                        var proxy = builder.of(OpenEndedParams.class);
                        map.put("days", daysPerTurn);
                        return proxy;
                    }
                };
                consumer.accept(bbuilder);
                return new Parameters(bbuilder.builder.getMap());
            }
        }
    }

    interface ChallengeAuth extends chariot.api.ChallengesAuth, ChallengeAuthCommon {
        Result<BulkPairing> createBulk(InternalBulkParameters params);

        default Result<BulkPairing> createBulk(Consumer<BulkBuilder> params) {
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
            public static InternalBulkParameters of(Consumer<BulkBuilder> params) {

                var bulkBuilder = new BulkBuilder() {
                    MapBuilder mapBuilder = new MapBuilder();
                    List<BulkParams.Pairing> pairings = new ArrayList<>();

                    @Override
                    public BulkParams clock(int initial, int increment) {
                        mapBuilder.addCustomHandler("addPairing", (args, map) -> {
                            pairings.add(BulkParams.Pairing.class.cast(args[0]));
                        });

                        var map = mapBuilder.getMap();
                        map.put("clock.limit", initial);
                        map.put("clock.increment", increment);
                        var bulkParams = mapBuilder.of(BulkParams.class);
                        return bulkParams;
                    }
                };

                params.accept(bulkBuilder);
                var map = bulkBuilder.mapBuilder.getMap();
                map.putIfAbsent("rated", false);
                map.put("players", new Pairings(bulkBuilder.pairings));
                return new Parameters(map);
            }

            record Pairings(List<BulkParams.Pairing> pairings) {
                @Override
                public String toString() {
                    return pairings.stream()
                        .map(p -> String.valueOf(p.tokenWhite().get()) + ":" + String.valueOf(p.tokenBlack().get()))
                        .collect(Collectors.joining(","));
                }
            }
        }
    }

    interface ChallengeAuthCommon extends chariot.api.ChallengesAuthCommon {

        Result<Challenge>   challenge(String userId, InternalChallengeParameters params);
        Result<ChallengeAI> challengeAI(InternalChallengeAIParameters params);
        Result<Ack>         cancelChallenge(String challengeId, Optional<Supplier<char[]>> opponentToken);
        Result<Ack>         declineChallenge(String challengeId, Optional<DeclineReason> reason);


        // "Implementation" of ChallengesAuthCommon
        default Result<Challenge> challenge(String userId, Consumer<ChallengeBuilder> params) {
            return challenge(userId, InternalChallengeParameters.of(params));
        }

        default Result<ChallengeAI> challengeAI(Consumer<ChallengeAIBuilder> consumer) {
            return challengeAI(InternalChallengeAIParameters.of(consumer));
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

            public static InternalChallengeParameters of(Consumer<ChallengeBuilder> consumer) {
                var bbuilder = new ChallengeBuilder() {
                    MapBuilder builder = new MapBuilder();

                    private void init() {
                        builder.addCustomHandler("acceptByToken", (args, map) -> {
                            map.put("acceptByToken", args[0]);
                            if (args.length == 2) {
                                map.put("message", args[1]);
                            }
                        });
                    }

                    @Override
                    public ChallengeParams clock(int initial, int increment) {
                        init();
                        var map = builder.getMap();
                        var proxy = builder.of(ChallengeParams.class);
                        map.put("clock.limit", initial);
                        map.put("clock.increment", increment);
                        return proxy;
                    }

                    @Override
                    public ChallengeParams daysPerTurn(int daysPerTurn) {
                        init();
                        var map = builder.getMap();
                        var proxy = builder.of(ChallengeParams.class);
                        map.put("days", daysPerTurn);
                        return proxy;
                    }
                };

                consumer.accept(bbuilder);
                return new Parameters(bbuilder.builder.getMap());
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
            public static InternalChallengeAIParameters of(Consumer<ChallengeAIBuilder> consumer) {
                var bbuilder = new ChallengeAIBuilder() {
                    MapBuilder builder = new MapBuilder();

                    @Override
                    public ChallengeAIParams clock(int initial, int increment) {
                        var map = builder.getMap();
                        var proxy = builder.of(ChallengeAIParams.class);
                        map.put("clock.limit", initial);
                        map.put("clock.increment", increment);
                        map.put("level", 1);
                        return proxy;
                    }

                    @Override
                    public ChallengeAIParams daysPerTurn(int daysPerTurn) {
                        var map = builder.getMap();
                        var proxy = builder.of(ChallengeAIParams.class);
                        map.put("days", daysPerTurn);
                        map.put("level", 1);
                        return proxy;
                    }
                };
                consumer.accept(bbuilder);
                return new Parameters(bbuilder.builder.getMap());
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

        default Result<Ack> move(String gameId, String move) { return move(gameId, move, Optional.empty()); }
        default Result<Ack> move(String gameId, String move, boolean drawOffer) { return move(gameId, move, Optional.of(drawOffer)); }
        default Result<Ack> seek(Consumer<SeekBuilder> consumer) { return seek(SeekParameters.of(consumer)); }

        sealed interface SeekParameters {
            public record Parameters(Map<String, Object> params) implements SeekParameters { }
            default public Map<String, Object> toMap() {
                if (this instanceof Parameters p)
                    return p.params();
                else
                    return Map.of();
            }

            public static SeekParameters of(Consumer<SeekBuilder> consumer) {
                var bbuilder = new SeekBuilder() {
                    MapBuilder builder = new MapBuilder();
                    @Override
                    public SeekParams clock(int initial, int increment) {
                        var map = builder.getMap();
                        var proxy = builder.of(SeekParams.class);
                        map.put("time", initial);
                        map.put("increment", increment);
                        return proxy;
                    }

                    @Override
                    public SeekParams daysPerTurn(int daysPerTurn) {
                        var map = builder.getMap();
                        var proxy = builder.of(SeekParams.class);
                        map.put("days", daysPerTurn);
                        return proxy;
                    }
                };
                consumer.accept(bbuilder);
                return new Parameters(bbuilder.builder.getMap());
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
        Result<Ack>       update(String tourId, InternalBroadcastParameters parameters);
        Result<Round>     createRound(String tourId, InternalRoundParameters parameters);
        Result<Round>     updateRound(String roundId, InternalRoundParameters parameters);

        default Result<Broadcast> create(Consumer<BroadcastBuilder> params) {
            return create(new InternalBroadcastParameters(true).of(BroadcastBuilder.class, params));
        }
        default Result<Round> createRound(String tourId, Consumer<RoundBuilder> params) {
            return createRound(tourId, new InternalRoundParameters().of(RoundBuilder.class, params));
        }
        default Result<Ack> update(String tourId, Consumer<BroadcastBuilder> params) {
            return update(tourId, new InternalBroadcastParameters(false).of(BroadcastBuilder.class, params));
        }
        default Result<Round> updateRound(String roundId, Consumer<RoundBuilder> params) {
            return updateRound(roundId, new InternalRoundParameters().of(RoundBuilder.class, params));
        }

        class InternalBroadcastParameters extends Base<InternalBroadcastParameters, BroadcastBuilder> {
            private boolean create = false;
            InternalBroadcastParameters(boolean create) {
                super();
                this.create = create;
            }

            @Override
            public MapBuilder addOverrides(MapBuilder builder) {
                builder.addCustomHandler("shortDescription", (args, map) -> { map.put("description", args[0]); });
                builder.addCustomHandler("longDescription", (args, map) -> { map.put("markup", args[0]); });
                return builder;
            }

            @Override
            public InternalBroadcastParameters toParams(Map<String,Object> map) {
                if (create) {
                    // We can provide default values for a new broadcast,
                    // but for an update - we don't want to overwrite the existing values.
                    // Currently an error is sent back from Lichess if these parameters are missing...
                    map.putIfAbsent("name", "No name");
                    map.putIfAbsent("description", "No description");
                }
                return super.toParams(map);
            }
        }

        class InternalRoundParameters extends Base<InternalRoundParameters, RoundBuilder> {}
    }
}
