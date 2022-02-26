package chariot.internal.impl;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import chariot.model.ArenaResult;
import chariot.model.Arena;
import chariot.model.Tournament;
import chariot.model.TournamentStatus;
import chariot.model.Game;
import chariot.model.Result;
import chariot.model.Swiss;
import chariot.model.SwissResult;
import chariot.model.TeamBattleResults;
import chariot.model.Enums.TournamentState;
import chariot.internal.Base;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;

public class TournamentsImpl extends Base implements Internal.Tournaments {

    TournamentsImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<Arena> arenaById(String arenaId, Optional<Integer> page) {
        var requestBuilder = Endpoint.tournamentArenaById.newRequest()
            .path(arenaId);
        page.ifPresent(v -> requestBuilder.query(Map.of("page", v)));
        var request = requestBuilder.build();
        return fetchOne(request);
    }

    @Override
    public Result<TournamentStatus> currentTournaments() {
        var request = Endpoint.tournamentArenas.newRequest()
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<ArenaResult> resultsByArenaId(String tournamentId, Optional<Integer> nb) {
        var builder = Endpoint.tournamentArenaResultsById.newRequest()
            .path(tournamentId);
        nb.ifPresent(n -> builder.query(Map.of("nb", n)));
        var request = builder.build();
        return fetchMany(request);
    }

    @Override
    public Result<Tournament> arenasCreatedByUserId(String userId, Set<TournamentState> specificStatus) {
        var builder = Endpoint.tournamentArenaCreatedByUser.newRequest();

        if (! specificStatus.isEmpty()) {
            builder.query(Map.of("status", specificStatus.stream().map(s -> String.valueOf(s.status())).toList().toArray(new String[0])));
        }

        var request = builder
            .path(userId)
            .build();

        return fetchMany(request);
    }

    @Override
    public Result<TeamBattleResults> teamBattleResultsById(String tournamentId) {
        var request = Endpoint.tournamentTeamBattleResultsById.newRequest()
            .path(tournamentId)
            .build();
        return fetchOne(request);
    }


    @Override
    public Result<Game> gamesByArenaId(String arenaId, TournamentParams parameters) {
        var request = Endpoint.gamesByArenaId.newRequest()
            .path(arenaId)
            .query(parameters.toMap())
            .build();
        return fetchMany(request);
    }

    @Override
    public Result<Swiss> swissById(String swissId) {

        var request = Endpoint.tournamentSwissById.newRequest()
            .path(swissId)
            .build();

        return fetchOne(request);
    }


    @Override
    public Result<SwissResult> resultsBySwissId(String swissId, Optional<Integer> nb) {
        var builder = Endpoint.swissResults.newRequest()
            .path(swissId);

        nb.ifPresent(n -> builder.query(Map.of("nb", n)));

        var request = builder.build();

        return fetchMany(request);
     }

    @Override
    public Result<String> swissTRF(String swissId) {

        var request = Endpoint.swissTRF.newRequest()
            .path(swissId)
            .build();

        return fetchMany(request);
    }

    @Override
    public Result<Game> gamesBySwissId(String swissId, TournamentParams parameters) {
        var request = Endpoint.gamesBySwissId.newRequest()
            .path(swissId)
            .query(parameters.toMap())
            .build();
        return fetchMany(request);
    }


}
