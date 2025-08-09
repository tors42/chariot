package chariot.internal.impl;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import chariot.api.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.Enums.*;
import chariot.model.*;

import java.util.function.Consumer;

import chariot.internal.RequestParameters.Params;

public class TournamentsHandler implements TournamentsApiAuth {

    private final RequestHandler requestHandler;

    public TournamentsHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public One<TournamentStatus> currentTournaments() {
        return Endpoint.tournamentArenas.newRequest(request -> {})
            .process(requestHandler);
    }

    @Override
    public One<Arena> arenaById(String arenaId, Consumer<StandingsParams> params) {
        var map = MapBuilder.of(StandingsParams.class).toMap(params);
        if (map.containsKey("pageSampleAll")) {
            return _arenaByIdAllPages(arenaId);
        }
        return Endpoint.tournamentArenaById.newRequest(request -> request
                .path(arenaId)
                .query(map))
            .process(requestHandler);
    }

    private One<Arena> _arenaByIdAllPages(String arenaId) {
        One<Arena> firstResult = _arenaByIdAndPage(arenaId, 1);
        if (! (firstResult instanceof Entry(Arena firstArena))) {
            if (firstResult instanceof Fail<Arena> f) return f;
            return One.fail(-1, "Failed to lookup " + arenaId + " page 1 standing");
        }
        int totalPlayers = firstArena.tourInfo().nbPlayers();
        int totalPages = (int) Math.ceil(totalPlayers / 10.0);
        Stream<Arena.Standing> totalStandings = firstArena.standings().stream();
        for (int page = 2; page <= totalPages; page++) {
            if (_arenaByIdAndPage(arenaId, page) instanceof Entry(Arena current)) {
                totalStandings = Stream.concat(totalStandings, current.standings().stream());
            } else {
                break;
            }
        }
        //    total = firstArena with { standings = totalStandings.toList(); };
        Arena total = withStandings(firstArena, totalStandings.toList());
        return One.entry(total);
    }

    static Arena withStandings(Arena arena, List<Arena.Standing> withStandings) {
        return new Arena(
            arena.tourInfo(),
            arena.duration(),
            arena.berserkable(),
            arena.conditions(),

            withStandings,

            arena.podium(),
            arena.teamStandings(),
            arena.topGames(),
            arena.pairingsClosed(),
            arena.isRecentlyFinished(),
            arena.spotlight(),
            arena.teamBattle(),
            arena.quote(),
            arena.greatPlayer(),
            arena.featured(),
            arena.stats());
    }

    private One<Arena> _arenaByIdAndPage(String arenaId, int page) {
        return Endpoint.tournamentArenaById.newRequest(request -> request
                .path(arenaId)
                .query(Map.of("page", page)))
            .process(requestHandler);
    }


    @Override
    public Many<ArenaResult> resultsByArenaId(String tournamentId, Consumer<ArenaResultParams> params) {
        return Endpoint.tournamentArenaResultsById.newRequest(request -> request
                .path(tournamentId)
                .query(MapBuilder.of(ArenaResultParams.class).rename("max", "nb").toMap(params)))
            .process(requestHandler);
    }

    @Override
    public Many<ArenaLight> arenasCreatedByUserId(String userId, Consumer<CreatedParams> params) {
        return Endpoint.tournamentArenaCreatedByUser.newRequest(request -> request
                .path(userId)
                .query(arenaByUserBuilderToMap(params)))
            .process(requestHandler);
     }

    @Override
    public Many<ArenaPlayed> arenasPlayedByUserId(String userId, Consumer<PlayedParams> params) {
        return Endpoint.tournamentArenaPlayedByUser.newRequest(request -> request
                .path(userId)
                .query(MapBuilder.of(PlayedParams.class)
                    .rename("max", "nb")
                    .toMap(params)))
            .process(requestHandler);
    }

    @Override
    public Many<Arena.TeamStanding> teamBattleResultsById(String tournamentId) {
        return Endpoint.tournamentTeamBattleResultsById.newRequest(request -> request
                .path(tournamentId))
            .process(requestHandler);
    }

    @Override
    public Many<Game> gamesByArenaId(String arenaId, Consumer<GamesApi.Filter> params) {
        return Endpoint.gamesByArenaId.newRequest(paramsConsumerByPathId(arenaId, params))
            .process(requestHandler);
    }

    @Override
    public Many<Pgn> pgnGamesByArenaId(String arenaId, Consumer<GamesApi.Filter> params) {
        return Endpoint.gamesByArenaIdPgn.newRequest(paramsConsumerByPathId(arenaId, params))
            .process(requestHandler);
    }

    @Override
    public One<Swiss> swissById(String swissId) {
        return Endpoint.tournamentSwissById.newRequest(request -> request
                .path(swissId))
            .process(requestHandler);
    }

    @Override
    public Many<SwissResult> resultsBySwissId(String swissId, Consumer<SwissResultParams> params) {
        return Endpoint.swissResults.newRequest(request -> request
                .path(swissId)
                .query(MapBuilder.of(SwissResultParams.class).rename("max", "nb").toMap(params)))
            .process(requestHandler);
    }

    @Override
    public Many<String> swissTRF(String swissId) {
        return Endpoint.swissTRF.newRequest(request -> request
                .path(swissId))
            .process(requestHandler);
    }

    @Override
    public Many<Game> gamesBySwissId(String swissId, Consumer<GamesApi.Filter> params) {
        return Endpoint.gamesBySwissId.newRequest(paramsConsumerByPathId(swissId, params))
            .process(requestHandler);
    }

    @Override
    public Many<Pgn> pgnGamesBySwissId(String swissId, Consumer<GamesApi.Filter> params) {
        return Endpoint.gamesBySwissIdPgn.newRequest(paramsConsumerByPathId(swissId, params))
            .process(requestHandler);
    }

    static Consumer<Params> paramsConsumerByPathId(String pathId, Consumer<GamesApi.Filter> params) {
        return request -> request
                .path(pathId)
                .query(GamesHandler.filterBuilder().toMap(params));
    }


    @Override
    public One<Arena> createArena(Consumer<ArenaBuilder> consumer) {
        return Endpoint.createArenaTournament.newRequest(request -> request
                .body(arenaBuilderToMap(consumer)))
            .process(requestHandler);
    }

    @Override
    public One<Arena> updateArena(String id, Consumer<ArenaBuilder> consumer) {
        return Endpoint.updateArenaTournament.newRequest(request -> request
                .path(id)
                .body(arenaBuilderToMap(consumer)))
            .process(requestHandler);
    }

    @Override
    public One<Arena> updateTeamBattle(String id, int nbLeaders, Set<String> teamIds) {
        return Endpoint.updateTeamBattleTournament.newRequest(request -> request
                .path(id)
                .body(Map.ofEntries(
                        Map.entry("teams", String.join(",", teamIds)),
                        Map.entry("nbLeaders", String.valueOf(nbLeaders))
                        )))
            .process(requestHandler);
    }

    @Override
    public Ack terminateArena(String id) {
        return Endpoint.terminateArenaTournament.newRequest(request -> request
                .path(id))
            .process(requestHandler);
    }

    @Override
    public Ack joinArena(String id, Consumer<JoinArenaParams> consumer) {
        return Endpoint.joinArenaTournament.newRequest(request -> request
            .path(id)
            .body(MapBuilder.of(JoinArenaParams.class)
                .rename("entryCode", "password").toMap(consumer)))
            .process(requestHandler);
    }

    @Override
    public Ack withdrawArena(String id) {
        return Endpoint.withdrawArenaTournament.newRequest(request -> request
            .path(id))
            .process(requestHandler);
    }

    @Override
    public One<Swiss> createSwiss(String teamId, Consumer<SwissBuilder> consumer) {
        return Endpoint.createSwiss.newRequest(request -> request
                .path(teamId)
                .body(swissBuilderToMap(consumer)))
            .process(requestHandler);
    }

    @Override
    public One<Swiss> updateSwiss(String id, Consumer<SwissBuilder> consumer) {
        return Endpoint.updateSwissTournament.newRequest(request -> request
                .path(id)
                .body(swissBuilderToMap(consumer)))
            .process(requestHandler);
    }

    @Override
    public Ack scheduleNextRoundSwiss(String id, ZonedDateTime date) {
        return Endpoint.scheduleNextRoundSwiss.newRequest(request -> request
                .path(id)
                .body(Map.of("date", date.toInstant().toEpochMilli())))
            .process(requestHandler);
    }


    @Override
    public Ack terminateSwiss(String swissId) {
        return Endpoint.terminateSwiss.newRequest(request -> request
                .path(swissId))
            .process(requestHandler);
    }

    @Override
    public Ack withdrawSwiss(String id) {
        return Endpoint.withdrawSwiss.newRequest(request -> request
            .path(id))
            .process(requestHandler);
    }

    @Override
    public Ack joinSwiss(String id, Consumer<JoinSwissParams> consumer) {
        return Endpoint.joinSwissTournament.newRequest(request -> request
                .path(id)
                .body(MapBuilder.of(JoinSwissParams.class)
                    .rename("entryCode", "password").toMap(consumer)))
            .process(requestHandler);
    }

    private Map<String, Object> arenaBuilderToMap(Consumer<ArenaBuilder> consumer) {
        var builder = withVariantHandling(withConditionsHandling(MapBuilder.of(ArenaParams.class)))
            .addCustomHandler("startTime", (args, map) -> {
                @SuppressWarnings("unchecked")
                var startTime = (Function<ArenaParams.StartTime.Provider, ArenaParams.StartTime>) args[0];
                var time = startTime.apply(ArenaParams.StartTime.provider());
                if (time instanceof ArenaParams.StartTime.InMinutes m) {
                    map.put("waitMinutes", m.waitMinutes());
                } else if (time instanceof ArenaParams.StartTime.AtDate d) {
                    map.put("startDate", d.startDate());
                }
            })
            .rename("withChat", "hasChat")
            .rename("entryCode", "password");


        var arenaBuilder = new ArenaBuilder() {
            @Override
            public ArenaParams clock(float initial, int increment) {
                return builder
                    .add("clockTime", initial)
                    .add("clockIncrement", increment)
                    .add("minutes", 60 + 40)
                    .proxy();
            }
        };
        consumer.accept(arenaBuilder);
        return builder.toMap();
    }

    private Map<String, Object> swissBuilderToMap(Consumer<SwissBuilder> consumer) {
        var builder = withVariantHandling(withConditionsHandling(MapBuilder.of(SwissParams.class)))
            .rename("entryCode", "password")
            .addCustomHandler("chatFor", (args, map) -> map.put("chatFor", ChatFor.class.cast(args[0]).id))
            .addCustomHandler("addForbiddenPairings", (args, map) -> {
                @SuppressWarnings("unchecked")
                var pairings = (Collection<SwissParams.Pairing>) args[0];

                String forbiddenPairings = pairings.stream()
                    .map(pairing -> String.join(" ", pairing.player1(), pairing.player2()))
                    .collect(Collectors.joining("\n"));

                var existingPairings = (String) map.get("forbiddenPairings");

                if (existingPairings != null) {
                    forbiddenPairings = String.join("\n", existingPairings, forbiddenPairings);
                }
                map.put("forbiddenPairings", forbiddenPairings);
            })
            .addCustomHandler("addManualPairings", (args, map) -> {
                @SuppressWarnings("unchecked")
                var pairings = (Collection<SwissParams.Pairing>) args[0];

                String manualPairings = pairings.stream()
                    .map(pairing -> String.join(" ", pairing.player1(), pairing.player2()))
                    .collect(Collectors.joining("\n"));

                var existingPairings = (String) map.get("manualPairings");

                if (existingPairings != null) {
                    manualPairings = String.join("\n", existingPairings, manualPairings);
                }
                map.put("manualPairings", manualPairings);
            });

        var swissBuilder = new SwissBuilder() {
            @Override
            public SwissParams clock(int initial, int increment) {
                return builder
                .add("clock.limit", initial)
                .add("clock.increment", increment)
                .add("nbRounds", 9)
                .proxy();
            }
        };
        consumer.accept(swissBuilder);
        return builder.toMap();
    }

    private static <T> MapBuilder<T> withConditionsHandling(MapBuilder<T> builder) {
        return builder
            .rename("conditionMinRating",     "conditions.minRating.rating")
            .rename("conditionMaxRating",     "conditions.maxRating.rating")
            .rename("conditionMinRatedGames", "conditions.nbRatedGame.nb")
            .rename("conditionPlayYourGames", "conditions.playYourGames")
            .rename("conditionAccountAge",    "conditions.accountAge")
            .rename("conditionTitled",        "conditions.titled")
            .rename("conditionBots",          "conditions.bots")
            .rename("conditionTeam",          "conditions.teamMember.teamId") // only in Arena
            .addCustomHandler("allowList", (args, map) -> {
                @SuppressWarnings("unchecked")
                Collection<String> allowList = (Collection<String>) args[0];
                if (!allowList.isEmpty()) map.put( "conditions.allowList", String.join(",", allowList));
            });
    }

    private static <T> MapBuilder<T> withVariantHandling(MapBuilder<T> builder) {
        return builder
            .addCustomHandler("variant", (args, map) -> {
                if (args[0] instanceof Variant variantType) {
                    switch(variantType) {
                        case Variant.Basic variant
                            -> map.put("variant", variant);
                        case Variant.Chess960(Some(var fen))
                            -> map.putAll(Map.of("variant", "chess960",
                                                 "position", fen));
                        case Variant.Chess960 __
                            -> map.put("variant", "chess960");
                        case Variant.FromPosition(Some(var fen), Some(var name))
                            when name.equals("workaround-standard")
                            -> map.putAll(Map.of("variant", "standard",
                                                 "position", fen));
                        case Variant.FromPosition(Some(var fen), var __)
                            -> map.putAll(Map.of("variant", "fromPosition",
                                                 "position", fen));
                        case Variant.FromPosition(var __, var ___)
                            -> map.put("variant", "fromPosition"); // hmmm
                    }
                }
            });
    }

    private Map<String, Object> arenaByUserBuilderToMap(Consumer<CreatedParams> consumer) {
        return MapBuilder.of(CreatedParams.class)
            .rename("max", "nb")
            .addCustomHandler("status", (args, map) -> {
                if (args.length > 0 && args[0] instanceof Object[] arr) {
                    map.put("status", Arrays.stream(arr)
                            .filter(TourInfo.Status.class::isInstance)
                            .map(TourInfo.Status.class::cast)
                            .map(status -> String.valueOf(status.status()))
                            .toArray(String[]::new));
                }
            })
        .toMap(consumer);
    }

}
