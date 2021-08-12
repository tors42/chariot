package chariot.internal.impl;

import java.util.Map;
import java.util.Optional;

import chariot.model.Team;
import chariot.model.Tournament;
import chariot.model.Result;
import chariot.model.PageTeam;
import chariot.model.Swiss;
import chariot.model.User;
import chariot.internal.Base;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;

public class TeamsImpl extends Base implements Internal.Teams {

    TeamsImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<Team> byTeamId(String teamId) {
        var request = Endpoint.teamById.newRequest()
            .path(teamId)
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<Team> byUserId(String userId) {
        var request = Endpoint.teamsByUserId.newRequest()
            .path(userId)
            .build();
        return fetchArr(request);
    }

    @Override
    public Result<User> usersByTeamId(String teamId) {
        var request = Endpoint.teamUsersById.newRequest()
            .path(teamId)
            .build();
        return fetchMany(request);
    }

    @Override
    public Result<PageTeam> byPage(int pageNumber) {
        var request = Endpoint.teamsByPage.newRequest()
            .query(Map.of("page", pageNumber))
            .build();
        return fetchOne(request);
     }

    @Override
    public Result<PageTeam> searchPage(String text, int pageNumber) {
        var request = Endpoint.teamsSearch.newRequest()
            .query(Map.of("text", text, "page", pageNumber))
            .build();
        return fetchOne(request);
     }

    @Override
    public Result<Tournament> arenaByTeamId(String teamId, Optional<Integer> max) {
        var requestBuilder = Endpoint.teamArenaById.newRequest()
            .path(teamId);
        max.ifPresent(v -> requestBuilder.query(Map.of("max", v)));
        var request = requestBuilder.build();
        return fetchMany(request);
    }

    @Override
    public Result<Swiss> swissByTeamId(String teamId, Optional<Integer> max) {
        var requestBuilder = Endpoint.teamSwissById.newRequest()
            .path(teamId);
        max.ifPresent(v -> requestBuilder.query(Map.of("max", v)));
        var request = requestBuilder.build();
        return fetchMany(request);
    }
}
