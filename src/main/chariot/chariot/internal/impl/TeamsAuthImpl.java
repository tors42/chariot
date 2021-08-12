package chariot.internal.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import chariot.model.Ack;
import chariot.model.Result;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;

public class TeamsAuthImpl extends TeamsImpl implements Internal.TeamsAuth {
    public TeamsAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<Ack> joinTeam(String teamId, Optional<String> message, Optional<String> password) {
        var requestBuilder = Endpoint.teamJoin.newRequest()
            .path(teamId);

        record KeyValue(String key, Optional<String> opt) {}
        var postData = Stream.of(new KeyValue("message", message), new KeyValue("password", password))
            .filter(e -> e.opt().isPresent())
            .map(e -> e.key() + "=" + URLEncoder.encode(e.opt().get(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));

        requestBuilder.post(postData);

        var request = requestBuilder.build();

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
}
