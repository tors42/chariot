package chariot.internal.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import chariot.model.Ack;
import chariot.model.Result;
import chariot.model.TeamRequest;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.internal.Util;

public class TeamsAuthImpl extends TeamsImpl implements Internal.TeamsAuth {
    public TeamsAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<Ack> joinTeam(String teamId, InternalJoinParams params) {
        var request = Endpoint.teamJoin.newRequest()
            .post(params.toMap())
            .path(teamId)
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<Ack> leaveTeam(String teamId) {
        var request = Endpoint.teamQuit.newRequest()
            .path(teamId)
            .post()
            .build();

        return fetchOne(request);
     }

    @Override
    public Result<Ack> kickFromTeam(String teamId, String userId) {
        var request = Endpoint.teamKick.newRequest()
            .path(teamId, userId)
            .post()
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<Ack> messageTeam(String teamId, String message) {
        var request = Endpoint.teamMessage.newRequest()
            .path(teamId)
            .post("message=" + URLEncoder.encode(message, StandardCharsets.UTF_8))
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<TeamRequest> requests(String teamId) {
        var request = Endpoint.teamRequests.newRequest()
            .path(teamId)
            .build();
        return fetchArr(request);
    }

    @Override
    public Result<Ack> requestAccept(String teamId, String userId) {
        var request = Endpoint.teamAcceptJoin.newRequest()
            .path(teamId, userId)
            .post()
            .build();

        return fetchOne(request);
     }

    @Override
    public Result<Ack> requestDecline(String teamId, String userId) {
        var request = Endpoint.teamDeclineJoin.newRequest()
            .path(teamId, userId)
            .post()
            .build();

        return fetchOne(request);
     }
}
