package chariot.internal.impl;

import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.model.Ack;
import chariot.model.Broadcast;
import chariot.model.Broadcast.Round;
import chariot.model.Result;

public class BroadcastsAuthImpl extends BroadcastsImpl implements Internal.BroadcastsAuth {

    public BroadcastsAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<Broadcast> broadcastById(String tourId) {
        var request = Endpoint.broadcastById.newRequest()
            .path(tourId)
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<Broadcast> create(InternalBroadcastParameters params) {
        var request = Endpoint.createBroadcast.newRequest()
            .post(params.toMap())
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<Ack> update(String tourId, InternalBroadcastParameters params) {
        var request = Endpoint.updateBroadcast.newRequest()
            .path(tourId)
            .post(params.toMap())
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<Round> roundById(String roundId) {
        var request = Endpoint.roundById.newRequest()
            .path(roundId)
            .build();

        return fetchOne(request);
     }

    @Override
    public Result<Broadcast.Round> createRound(String tourId, InternalRoundParameters params) {
        var request = Endpoint.createRound.newRequest()
            .path(tourId)
            .post(params.toMap())
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<Broadcast.Round> updateRound(String roundId, InternalRoundParameters params) {
        var request = Endpoint.updateRound.newRequest()
            .path(roundId)
            .post(params.toMap())
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<Ack> pushPgnByRoundId(String roundId, String pgn) {
        var request = Endpoint.pushPGNbyRoundId.newRequest()
            .path(roundId)
            .post(pgn)
            .build();

        return fetchOne(request);
    }

}
