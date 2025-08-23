package chariot.internal.impl;

import java.util.*;
import java.util.function.Consumer;

import chariot.api.*;
import chariot.model.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import java.util.stream.*;

public class TeamsHandler implements TeamsApiAuth {
    private final RequestHandler requestHandler;

    public TeamsHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public One<Team> byTeamId(String teamId) {
        return Endpoint.teamById.newRequest(request -> request
                .path(teamId))
            .process(requestHandler);
    }

    @Override
    public Many<Team> byUserId(String userId) {
        return Endpoint.teamsByUserId.newRequest(request -> request
                .path(userId))
            .process(requestHandler);
    }

    @Override
    public Many<TeamMember> usersByTeamId(String teamId) {
        var result = Endpoint.teamUsersById.newRequest(request -> request
                .path(teamId)
                )
            .process(requestHandler);

        if (! (result instanceof Entries<TeamMember> many)) return result;

        return Many.entries(many.stream()
                .map(member -> new TeamMember(member.user(), teamId, member.joinedTeamAt())));
    }

    @Override
    public Many<TeamMemberFull> usersByTeamIdFull(String teamId) {
        var result = Endpoint.teamUsersFullById.newRequest(request -> request
                .path(teamId)
                .query(Map.of("full", 1))
                )
            .process(requestHandler);

        if (! (result instanceof Entries<TeamMemberFull> many)) return result;

        return Many.entries(many.stream()
                .map(member -> new TeamMemberFull(member.user(), teamId, member.joinedTeamAt())));
    }


    @Override
    public One<PageTeam> popularTeamsByPage(int page) {
        return Endpoint.popularTeamsByPage.newRequest(request -> request
                .query(Map.of("page", page)))
            .process(requestHandler);
    }

    @Override
    public One<PageTeam> popularTeamsByPage() {
        return Endpoint.popularTeamsByPage.newRequest(request -> {})
            .process(requestHandler);
    }

    @Override
    public One<PageTeam> searchByPage(Consumer<PageParams> consumer) {
        return Endpoint.teamsSearch.newRequest(request -> request
                .query(MapBuilder.of(PageParams.class).toMap(consumer)))
            .process(requestHandler);
     }

    @Override
    public Many<ArenaLight> arenaByTeamId(String teamId, Consumer<TournamentParams> params) {
        return Endpoint.teamArenaById.newRequest(request -> request
                .path(teamId)
                .query(MapBuilder.of(TournamentParams.class).toMap(params)))
            .process(requestHandler);
    }

    @Override
    public Many<Swiss> swissByTeamId(String teamId, Consumer<TournamentParams> params) {
        return Endpoint.teamSwissById.newRequest(request -> request
                .path(teamId)
                .query(MapBuilder.of(TournamentParams.class).toMap(params)))
            .process(requestHandler);
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
        if (firstPage instanceof Some<PageTeam> one) {
            var spliterator = Util.PageSpliterator.of(one.value(),
                    pageNum -> searchByPage(consumer.andThen(p -> p.page(pageNum))) instanceof Some<PageTeam> pt ?
                    pt.value() : new PageTeam(0,0,List.of(),0,0,0,0));
            return Many.entries(StreamSupport.stream(spliterator, false));
        } else {
            return Many.entries(Stream.of());
        }
    }

    @Override
    public Many<Team> popularTeams() {
        var firstPage = popularTeamsByPage(1);
        if (firstPage instanceof Some<PageTeam> one) {
            var spliterator = Util.PageSpliterator.of(one.value(),
                    pageNum -> popularTeamsByPage(pageNum) instanceof Some<PageTeam> pt ?
                    pt.value() : new PageTeam(0,0,List.of(),0,0,0,0));
            return Many.entries(StreamSupport.stream(spliterator, false));
        } else {
            return Many.entries(Stream.of());
        }
    }

    @Override
    public Ack joinTeam(String teamId, Consumer<JoinParams> consumer) {
        return Endpoint.teamJoin.newRequest(request -> request
                .body(MapBuilder.of(JoinParams.class)
                    .rename("entryCode", "password")
                    .toMap(consumer))
                .path(teamId))
            .process(requestHandler);
    }

    @Override
    public Ack leaveTeam(String teamId) {
        return Endpoint.teamQuit.newRequest(request -> request
                .path(teamId))
            .process(requestHandler);
     }

    @Override
    public Ack kickFromTeam(String teamId, String userId) {
        return Endpoint.teamKick.newRequest(request -> request
                .path(teamId, userId))
            .process(requestHandler);
    }

    @Override
    public Ack messageTeam(String teamId, String message) {
        return Endpoint.teamMessage.newRequest(request -> request
                .path(teamId)
                .body(Map.of("message", message)))
            .process(requestHandler);
    }

    @Override
    public Many<TeamRequest> requests(String teamId) {
        return Endpoint.teamRequests.newRequest(request -> request
                .path(teamId))
            .process(requestHandler);
    }

    @Override
    public Many<TeamRequest> requestsDeclined(String teamId) {
        return Endpoint.teamRequests.newRequest(request -> request
                .path(teamId)
                .query(Map.of("declined", true)))
            .process(requestHandler);
    }

    @Override
    public Ack requestAccept(String teamId, String userId) {
        return Endpoint.teamAcceptJoin.newRequest(request -> request
                .path(teamId, userId))
            .process(requestHandler);
     }

    @Override
    public Ack requestDecline(String teamId, String userId) {
        return Endpoint.teamDeclineJoin.newRequest(request -> request
                .path(teamId, userId))
            .process(requestHandler);
     }
}
