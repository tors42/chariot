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

import chariot.model.Enums.TournamentState;
import chariot.internal.RequestParameters.Params;

public class TournamentsHandler implements TournamentsAuth {

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
    public One<Arena> arenaById(String arenaId, int page) {
        return Endpoint.tournamentArenaById.newRequest(request -> request
                .path(arenaId)
                .query(Map.of("page", page)))
            .process(requestHandler);
    }

    @Override
    public One<Arena> arenaById(String arenaId) {
        return Endpoint.tournamentArenaById.newRequest(request -> request
                .path(arenaId))
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
    public Many<Tournament> arenasCreatedByUserId(String userId, Set<TournamentState> specificStatus) {
        Consumer<Params> params = request -> request.path(userId);
        if (! specificStatus.isEmpty()) {
            params = params.andThen(request -> request.query(Map.of("status", specificStatus.stream()
                            .map(s -> String.valueOf(s.status()))
                            .toArray(String[]::new))));
        }
        return Endpoint.tournamentArenaCreatedByUser.newRequest(params)
            .process(requestHandler);
    }

    @Override
    public One<TeamBattleResults> teamBattleResultsById(String tournamentId) {
        return Endpoint.tournamentTeamBattleResultsById.newRequest(request -> request
                .path(tournamentId))
            .process(requestHandler);
    }

    @Override
    public Many<Game> gamesByArenaId(String arenaId, Consumer<Games.Filter> params) {
        return Endpoint.gamesByArenaId.newRequest(paramsConsumerByPathId(arenaId, params))
            .process(requestHandler);
    }

    @Override
    public Many<Pgn> pgnGamesByArenaId(String arenaId, Consumer<Games.Filter> params) {
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
    public Many<Game> gamesBySwissId(String swissId, Consumer<Games.Filter> params) {
        return Endpoint.gamesBySwissId.newRequest(paramsConsumerByPathId(swissId, params))
            .process(requestHandler);
    }

    @Override
    public Many<Pgn> pgnGamesBySwissId(String swissId, Consumer<Games.Filter> params) {
        return Endpoint.gamesBySwissIdPgn.newRequest(paramsConsumerByPathId(swissId, params))
            .process(requestHandler);
    }

    static Consumer<Params> paramsConsumerByPathId(String pathId, Consumer<Games.Filter> params) {
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
    public One<Void> terminateArena(String id) {
        return Endpoint.terminateArenaTournament.newRequest(request -> request
                .path(id))
            .process(requestHandler);
    }

    @Override
    public One<Void> joinArena(String id, Consumer<JoinArenaParams> consumer) {
        return Endpoint.joinArenaTournament.newRequest(request -> request
            .path(id)
            .body(MapBuilder.of(JoinArenaParams.class)
                .rename("entryCode", "password").toMap(consumer)))
            .process(requestHandler);
    }

    @Override
    public One<Void> withdrawArena(String id) {
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
    public One<Void> scheduleNextRoundSwiss(String id, ZonedDateTime date) {
        return Endpoint.scheduleNextRoundSwiss.newRequest(request -> request
                .path(id)
                .body(Map.of("date", date.toInstant().toEpochMilli())))
            .process(requestHandler);
    }


    @Override
    public One<Void> terminateSwiss(String swissId) {
        return Endpoint.terminateSwiss.newRequest(request -> request
                .path(swissId))
            .process(requestHandler);
    }

    @Override
    public One<Void> withdrawSwiss(String id) {
        return Endpoint.withdrawSwiss.newRequest(request -> request
            .path(id))
            .process(requestHandler);
    }

    @Override
    public One<Void> joinSwiss(String id, Consumer<JoinSwissParams> consumer) {
        return Endpoint.joinSwissTournament.newRequest(request -> request
                .path(id)
                .body(MapBuilder.of(JoinSwissParams.class)
                    .rename("entryCode", "password").toMap(consumer)))
            .process(requestHandler);
    }

    private Map<String, Object> arenaBuilderToMap(Consumer<ArenaBuilder> consumer) {
        var builder = MapBuilder.of(ArenaParams.class)
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
            .rename("entryCode", "password")
            .rename("conditionTeam", "conditions.teamMember.teamId")
            .rename("conditionMinRating", "conditions.minRating.rating")
            .rename("conditionMaxRating", "conditions.maxRating.rating")
            .rename("conditionMinRatedGames", "conditions.nbRatedGame.nb")
            .rename("conditionAccountAge", "conditions.accountAge")
            .addCustomHandler("allowList", (args, map) -> {
                @SuppressWarnings("unchecked")
                List<String> list = (List<String>) args[0];
                if (!list.isEmpty()) map.put("conditions.allowList", String.join(",", list));
            });

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
        var builder = MapBuilder.of(SwissParams.class)
            .rename("entryCode", "password")
            .rename("conditionMinRating", "conditions.minRating.rating")
            .rename("conditionMaxRating", "conditions.maxRating.rating")
            .rename("conditionMinRatedGames", "conditions.nbRatedGame.nb")
            .rename("conditionPlayYourGames", "conditions.playYourGames")
            .rename("conditionAccountAge", "conditions.accountAge")
            .addCustomHandler("chatFor", (args, map) -> map.put("chatFor", ChatFor.class.cast(args[0]).id))
            .addCustomHandler("allowList", (args, map) -> {
                @SuppressWarnings("unchecked")
                List<String> list = (List<String>) args[0];
                if (!list.isEmpty()) map.put("conditions.allowList", String.join(",", list));
            })
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
}
