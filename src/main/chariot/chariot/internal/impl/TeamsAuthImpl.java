package chariot.internal.impl;

import java.util.*;
import java.util.function.Consumer;

import chariot.api.*;
import chariot.model.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;

public class TeamsAuthImpl extends TeamsImpl implements TeamsAuth {
    public TeamsAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public One<Ack> joinTeam(String teamId, Consumer<JoinParams> consumer) {
        return Endpoint.teamJoin.newRequest(request -> request
                .body(MapBuilder.of(JoinParams.class)
                    .rename("entryCode", "password")
                    .toMap(consumer))
                .path(teamId))
            .process(this);
    }

    @Override
    public One<Ack> leaveTeam(String teamId) {
        return Endpoint.teamQuit.newRequest(request -> request
                .path(teamId))
            .process(this);
     }

    @Override
    public One<Ack> kickFromTeam(String teamId, String userId) {
        return Endpoint.teamKick.newRequest(request -> request
                .path(teamId, userId))
            .process(this);
    }

    @Override
    public One<Ack> messageTeam(String teamId, String message) {
        return Endpoint.teamMessage.newRequest(request -> request
                .path(teamId)
                .body(Map.of("message", message)))
            .process(this);
    }

    @Override
    public Many<TeamRequest> requests(String teamId) {
        return Endpoint.teamRequests.newRequest(request -> request
                .path(teamId))
            .process(this);
    }

    @Override
    public One<Ack> requestAccept(String teamId, String userId) {
        return Endpoint.teamAcceptJoin.newRequest(request -> request
                .path(teamId, userId))
            .process(this);
     }

    @Override
    public One<Ack> requestDecline(String teamId, String userId) {
        return Endpoint.teamDeclineJoin.newRequest(request -> request
                .path(teamId, userId))
            .process(this);
     }
}
