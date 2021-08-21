package chariot.internal.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import chariot.model.Team;
import chariot.model.Tournament;
import chariot.model.Result;
import chariot.model.PageTeam;
import chariot.model.Swiss;
import chariot.model.User;
import chariot.internal.Base;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.internal.Util;

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
    public Result<PageTeam> popularTeamsByPage(Optional<Integer> page) {
        var map = new HashMap<String, Object>() {{
            page.ifPresent(p -> put("page", p));
        }};

        var request = Endpoint.popularTeamsByPage.newRequest()
            .query(map)
            .build();

        return fetchOne(request);
     }

    @Override
    public Result<PageTeam> searchByPage(Optional<Integer> page, Optional<String> text) {
        var map = new HashMap<String, Object>() {{
            page.ifPresent(p -> put("page", p));
            text.ifPresent(t -> put("text", t));
        }};

        var request = Endpoint.teamsSearch.newRequest()
            .query(map)
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

    @Override
    public Result<Team> search(Optional<String> text) {

        var firstPage = searchByPage(Optional.of(1), text);

        if (firstPage instanceof Result.One<PageTeam> one) {
            var page = one.entry();
            var empty = new PageTeam(0,0,List.of(),0,0,0,0);
            var spliterator = Util.PageSpliterator.of(page, (pageNum) -> searchByPage(Optional.of(pageNum), text).getOrElse(empty));
            var teamStream = StreamSupport.stream(spliterator, false);
            return Result.many(teamStream);
        } else {
            return Result.many(Stream.of());
        }

    }

    @Override
    public Result<Team> popularTeams() {

        var firstPage = popularTeamsByPage(Optional.of(1));

        if (firstPage instanceof Result.One<PageTeam> one) {
            var page = one.entry();
            var empty = new PageTeam(0,0,List.of(),0,0,0,0);
            var spliterator = Util.PageSpliterator.of(page, (pageNum) -> popularTeamsByPage(Optional.of(pageNum)).getOrElse(empty));
            var teamStream = StreamSupport.stream(spliterator, false);
            return Result.many(teamStream);
        } else {
            return Result.many(Stream.of());
        }

    }

}
