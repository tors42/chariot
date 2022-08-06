package chariot.internal.impl;

import java.util.Map;

import chariot.api.*;
import chariot.internal.*;
import chariot.model.Broadcast;
import chariot.model.Pgn;

public class BroadcastsImpl extends Base implements Broadcasts {

    public BroadcastsImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Many<Broadcast> official(int  nb) {
        return Endpoint.officialBroadcasts.newRequest(request -> request
                .query(Map.of("nb", nb)))
            .process(this);
    }

    @Override
    public Many<Broadcast> official() {
        return Endpoint.officialBroadcasts.newRequest(request -> {})
            .process(this);
    }

    @Override
    public Many<Pgn> streamBroadcast(String roundId) {
        return Endpoint.streamBroadcast.newRequest(request -> request
                .path(roundId)
                .stream())
            .process(this);
    }

    @Override
    public Many<Pgn> exportOneRoundPgn(String roundId) {
        return Endpoint.exportBroadcastOneRoundPgn.newRequest(request -> request
                .path(roundId))
            .process(this);
    }

    @Override
    public Many<Pgn> exportPgn(String tourId) {
        return Endpoint.exportBroadcastAllRoundsPgn.newRequest(request -> request
                .path(tourId))
            .process(this);
    }
}
