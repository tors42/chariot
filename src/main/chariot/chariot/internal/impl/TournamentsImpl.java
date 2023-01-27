package chariot.internal.impl;

import java.util.*;
import java.util.function.Consumer;

import chariot.api.*;
import chariot.model.*;
import chariot.model.Enums.TournamentState;
import chariot.internal.*;
import chariot.internal.RequestParameters.Params;
import chariot.internal.Util.MapBuilder;

public class TournamentsImpl extends Base implements Tournaments {

    TournamentsImpl(InternalClient client) {
        super(client);
    }

    @Override
    public One<TournamentStatus> currentTournaments() {
        return Endpoint.tournamentArenas.newRequest(request -> {})
            .process(this);
    }

    @Override
    public One<Arena> arenaById(String arenaId, int page) {
        return Endpoint.tournamentArenaById.newRequest(request -> request
                .path(arenaId)
                .query(Map.of("page", page)))
            .process(this);
    }

    @Override
    public One<Arena> arenaById(String arenaId) {
        return Endpoint.tournamentArenaById.newRequest(request -> request
                .path(arenaId))
            .process(this);
    }

    @Override
    public Many<ArenaResult> resultsByArenaId(String tournamentId, Consumer<ArenaResultParams> params) {
        return Endpoint.tournamentArenaResultsById.newRequest(request -> request
                .path(tournamentId)
                .query(MapBuilder.of(ArenaResultParams.class).rename("max", "nb").toMap(params)))
            .process(this);
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
            .process(this);
    }

    @Override
    public One<TeamBattleResults> teamBattleResultsById(String tournamentId) {
        return Endpoint.tournamentTeamBattleResultsById.newRequest(request -> request
                .path(tournamentId))
            .process(this);
    }

    @Override
    public Many<Game> gamesByArenaId(String arenaId, Consumer<Games.Filter> params) {
        return Endpoint.gamesByArenaId.newRequest(paramsConsumerByPathId(arenaId, params))
            .process(this);
    }

    @Override
    public Many<Pgn> pgnGamesByArenaId(String arenaId, Consumer<Games.Filter> params) {
        return Endpoint.gamesByArenaIdPgn.newRequest(paramsConsumerByPathId(arenaId, params))
            .process(this);
    }

    @Override
    public One<Swiss> swissById(String swissId) {
        return Endpoint.tournamentSwissById.newRequest(request -> request
                .path(swissId))
            .process(this);
    }

    @Override
    public Many<SwissResult> resultsBySwissId(String swissId, Consumer<SwissResultParams> params) {
        return Endpoint.swissResults.newRequest(request -> request
                .path(swissId)
                .query(MapBuilder.of(SwissResultParams.class).rename("max", "nb").toMap(params)))
            .process(this);
    }

    @Override
    public Many<String> swissTRF(String swissId) {
        return Endpoint.swissTRF.newRequest(request -> request
                .path(swissId))
            .process(this);
    }

    @Override
    public Many<Game> gamesBySwissId(String swissId, Consumer<Games.Filter> params) {
        return Endpoint.gamesBySwissId.newRequest(paramsConsumerByPathId(swissId, params))
            .process(this);
    }

    @Override
    public Many<Pgn> pgnGamesBySwissId(String swissId, Consumer<Games.Filter> params) {
        return Endpoint.gamesBySwissIdPgn.newRequest(paramsConsumerByPathId(swissId, params))
            .process(this);
    }

    static Consumer<Params> paramsConsumerByPathId(String pathId, Consumer<Games.Filter> params) {
        return request -> request
                .path(pathId)
                .query(GamesImpl.filterBuilder().toMap(params));
    }

}
