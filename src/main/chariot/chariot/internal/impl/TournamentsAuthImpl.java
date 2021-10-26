package chariot.internal.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.internal.Util;
import chariot.model.Ack;
import chariot.model.Arena;
import chariot.model.Result;
import chariot.model.Swiss;

public class TournamentsAuthImpl extends TournamentsImpl implements Internal.TournamentsAuth {

    public TournamentsAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<Arena> createArena(ArenaParameters parameters) {
        var requestBuilder = Endpoint.createArenaTournament.newRequest();

        if (parameters instanceof ArenaParameters.Parameters p) {
            var postData = p.params().entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(String.valueOf(e.getValue()), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
            requestBuilder.post(postData);
        }

        var request = requestBuilder.build();
        return fetchOne(request);
    }

    @Override
    public Result<Arena> updateArena(String id, ArenaParameters parameters) {
        var requestBuilder = Endpoint.updateArenaTournament.newRequest()
            .path(id);

        if (parameters instanceof ArenaParameters.Parameters p) {
            var postData = p.params().entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(String.valueOf(e.getValue()), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
            requestBuilder.post(postData);
        }

        var request = requestBuilder.build();
        return fetchOne(request);
    }

    @Override
    public Result<Arena> updateTeamBattle(String id, int nbLeaders, Set<String> teamIds) {
        var requestBuilder = Endpoint.updateTeamBattleTournament.newRequest()
            .path(id);

        record KeyValue(String key, String value) {};
        var postData = Stream.of(
                new KeyValue("teams", teamIds.stream().collect(Collectors.joining(","))),
                new KeyValue("nbLeaders", String.valueOf(nbLeaders))
                )
            .map(kv -> kv.key() + "=" + kv.value())
            .collect(Collectors.joining("&"));

        requestBuilder.post(postData);

        var request = requestBuilder.build();
        return fetchOne(request);
    }

    @Override
    public Result<Ack> terminateArena(String id) {
        var request = Endpoint.terminateArenaTournament.newRequest()
            .path(id)
            .post()
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<Ack> joinArena(String id, Optional<String> password, Optional<String> teamId) {
        var requestBuilder = Endpoint.joinArenaTournament.newRequest()
            .path(id);

        record KeyValue(String key, Optional<String> opt) {}
        var postData = Stream.of(new KeyValue("password", password), new KeyValue("team", teamId))
            .filter(e -> e.opt().isPresent())
            .map(e -> e.key() + "=" + URLEncoder.encode(e.opt().get(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));

        requestBuilder.post(postData);

        var request = requestBuilder.build();

        return fetchOne(request);
    }

    @Override
    public Result<Ack> withdrawArena(String id) {
        var request = Endpoint.withdrawArenaTournament.newRequest()
            .path(id)
            .post()
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<Swiss> createSwiss(String teamId, SwissParameters parameters) {
        var parametersString = Util.urlEncode(parameters.toMap());

        var request = Endpoint.createSwiss.newRequest()
            .path(teamId)
            .post(parametersString)
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<Ack> terminateSwiss(String swissId) {
        var request = Endpoint.terminateSwiss.newRequest()
            .path(swissId)
            .post()
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<Ack> joinSwiss(String id, Optional<String> password) {
        var requestBuilder = Endpoint.joinSwissTournament.newRequest()
            .path(id);

        password.ifPresent(pw -> requestBuilder.post(Util.urlEncode(Map.of("password", pw))));

        var request = requestBuilder.build();

        return fetchOne(request);
    }



}
