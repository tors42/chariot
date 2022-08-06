package chariot.internal.impl;

import java.util.Map;
import java.util.function.Consumer;

import chariot.api.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.Ack;
import chariot.model.Broadcast;
import chariot.model.Broadcast.Round;

public class BroadcastsAuthImpl extends BroadcastsImpl implements BroadcastsAuth {

    public BroadcastsAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public One<Broadcast> broadcastById(String tourId) {
        return Endpoint.broadcastById.newRequest(request -> request
                .path(tourId))
            .process(this);
    }

    @Override
    public One<Broadcast> create(Consumer<BroadcastBuilder> params) {
        return Endpoint.createBroadcast.newRequest(request -> request
                .post(broadastBuilderToMap(params)))
            .process(this);
    }

    @Override
    public One<Ack> update(String tourId, Consumer<BroadcastBuilder> params) {
        return Endpoint.updateBroadcast.newRequest(request -> request
                .path(tourId)
                .post(broadastBuilderToMap(params)))
            .process(this);
    }

    @Override
    public One<Round> roundById(String roundId) {
        return Endpoint.roundById.newRequest(request -> request
                .path(roundId))
            .process(this);
     }

    @Override
    public One<Broadcast.Round> createRound(String tourId, Consumer<RoundBuilder> params) {
        return Endpoint.createRound.newRequest(request -> request
                .path(tourId)
                .post(MapBuilder.of(RoundBuilder.class).toMap(params)))
            .process(this);
    }

    @Override
    public One<Broadcast.Round> updateRound(String roundId, Consumer<RoundBuilder> params) {
        return Endpoint.updateRound.newRequest(request -> request
                .path(roundId)
                .post(MapBuilder.of(RoundBuilder.class).toMap(params)))
            .process(this);
    }

    @Override
    public One<Ack> pushPgnByRoundId(String roundId, String pgn) {
        return Endpoint.pushPGNbyRoundId.newRequest(request -> request
                .path(roundId)
                .post(pgn))
            .process(this);
    }

    private Map<String, Object> broadastBuilderToMap(Consumer<BroadcastBuilder> consumer) {
        return MapBuilder.of(BroadcastBuilder.class)
                    .rename("shortDescription", "description")
                    .rename("longDescription", "markdown")
                    .toMap(consumer);
    }
}
