package chariot.internal.impl;

import java.util.*;
import java.util.function.Consumer;

import chariot.api.*;
import chariot.model.*;
import chariot.model.Enums.TournamentState;
import chariot.internal.*;
import chariot.internal.RequestParameters.Params;

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
    public Many<ArenaResult> resultsByArenaId(String tournamentId, int nb) {
        return Endpoint.tournamentArenaResultsById.newRequest(request -> request
                .path(tournamentId)
                .query(Map.of("nb", nb)))
            .process(this);
    }

    @Override
    public Many<ArenaResult> resultsByArenaId(String tournamentId) {
        return Endpoint.tournamentArenaResultsById.newRequest(request -> request
                .path(tournamentId))
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
        return Endpoint.gamesByArenaId.newRequest(request -> request
                .path(arenaId)
                .query(GamesImpl.filterBuilder().toMap(params)))
            .process(this);
    }

    @Override
    public One<Swiss> swissById(String swissId) {
        return Endpoint.tournamentSwissById.newRequest(request -> request
                .path(swissId))
            .process(this);
    }

    @Override
    public Many<SwissResult> resultsBySwissId(String swissId, int nb) {
        return Endpoint.swissResults.newRequest(request -> request
                .path(swissId)
                .query(Map.of("nb", nb)))
            .process(this);
    }

    @Override
    public Many<SwissResult> resultsBySwissId(String swissId) {
        return Endpoint.swissResults.newRequest(request -> request
                .path(swissId))
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
        return Endpoint.gamesBySwissId.newRequest(request -> request
                .path(swissId)
                .query(GamesImpl.filterBuilder().toMap(params)))
            .process(this);
    }
}
