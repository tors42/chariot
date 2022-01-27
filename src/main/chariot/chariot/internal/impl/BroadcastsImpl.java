package chariot.internal.impl;

import java.util.Map;
import java.util.Optional;

import chariot.internal.Base;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.model.Broadcast;
import chariot.model.Result;

public class BroadcastsImpl extends Base implements Internal.Broadcasts {

    public BroadcastsImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<Broadcast> official(Optional<Integer> nb) {
        var builder = Endpoint.officialBroadcasts.newRequest();
        nb.ifPresent(n -> builder.query(Map.of("nb", n)));
        var request = builder.build();
        return fetchMany(request);
     }

    @Override
    public Result<String> streamBroadcast(String roundId) {
        var request = Endpoint.streamBroadcast.newRequest()
            .path(roundId)
            .stream()
            .build();
        return fetchMany(request);
    }

    @Override
    public Result<String> exportOneRoundPgn(String roundId) {
        var request = Endpoint.exportBroadcastOneRoundPgn.newRequest()
            .path(roundId)
            .build();
        return fetchMany(request);
    }

    @Override
    public Result<String> exportPgn(String tourId) {
        var request = Endpoint.exportBroadcastAllRoundsPgn.newRequest()
            .path(tourId)
            .build();
        return fetchMany(request);
    }

}