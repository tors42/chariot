package chariot.internal.impl;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

import chariot.api.*;
import chariot.model.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;

public class TeamsImpl extends Base implements Teams {

    TeamsImpl(InternalClient client) {
        super(client);
    }

    @Override
    public One<Team> byTeamId(String teamId) {
        return Endpoint.teamById.newRequest(request -> request
                .path(teamId))
            .process(this);
    }

    @Override
    public Many<Team> byUserId(String userId) {
        return Endpoint.teamsByUserId.newRequest(request -> request
                .path(userId))
            .process(this);
    }

    @Override
    public Many<User> usersByTeamId(String teamId) {
        return Endpoint.teamUsersById.newRequest(request -> request
                .path(teamId))
            .process(this);
    }

    @Override
    public One<PageTeam> popularTeamsByPage(int page) {
        return Endpoint.popularTeamsByPage.newRequest(request -> request
                .query(Map.of("page", page)))
            .process(this);
    }

    @Override
    public One<PageTeam> popularTeamsByPage() {
        return Endpoint.popularTeamsByPage.newRequest(request -> {})
            .process(this);
    }

    @Override
    public One<PageTeam> searchByPage(Consumer<PageParams> consumer) {
        return Endpoint.teamsSearch.newRequest(request -> request
                .query(MapBuilder.of(PageParams.class).toMap(consumer)))
            .process(this);
     }

    @Override
    public Many<Tournament> arenaByTeamId(String teamId, int max) {
        return Endpoint.teamArenaById.newRequest(request -> request
                .path(teamId)
                .query(Map.of("max", max)))
            .process(this);
    }

    @Override
    public Many<Tournament> arenaByTeamId(String teamId) {
        return Endpoint.teamArenaById.newRequest(request -> request
                .path(teamId))
            .process(this);
    }

    @Override
    public Many<Swiss> swissByTeamId(String teamId, int max) {
        return Endpoint.teamSwissById.newRequest(request -> request
                .path(teamId)
                .query(Map.of("max", max)))
            .process(this);
    }

    @Override
    public Many<Swiss> swissByTeamId(String teamId) {
        return Endpoint.teamSwissById.newRequest(request -> request
                .path(teamId))
            .process(this);
    }

    @Override
    public Many<Team> search(String text) {
        return search(p -> p.text(text));
    }

    @Override
    public Many<Team> search() {
        return search(__ -> {});
    }

    private Many<Team> search(Consumer<PageParams> consumer) {
        var firstPage = searchByPage(consumer.andThen(p -> p.page(1)));
        if (firstPage instanceof Entry<PageTeam> one) {
            var spliterator = Util.PageSpliterator.of(one.entry(),
                    pageNum -> searchByPage(consumer.andThen(p -> p.page(pageNum))) instanceof Entry<PageTeam> pt ?
                    pt.entry() : new PageTeam(0,0,List.of(),0,0,0,0));
            return Many.entries(StreamSupport.stream(spliterator, false));
        } else {
            return Many.entries(Stream.of());
        }
    }

    @Override
    public Many<Team> popularTeams() {
        var firstPage = popularTeamsByPage(1);
        if (firstPage instanceof Entry<PageTeam> one) {
            var spliterator = Util.PageSpliterator.of(one.entry(),
                    pageNum -> popularTeamsByPage(pageNum) instanceof Entry<PageTeam> pt ?
                    pt.entry() : new PageTeam(0,0,List.of(),0,0,0,0));
            return Many.entries(StreamSupport.stream(spliterator, false));
        } else {
            return Many.entries(Stream.of());
        }
    }
}
