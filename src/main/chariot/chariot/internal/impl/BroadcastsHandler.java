package chariot.internal.impl;

import java.util.Map;
import java.util.function.Consumer;

import chariot.api.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.*;
import chariot.model.Broadcast.Round;

public class BroadcastsHandler implements BroadcastsAuth {

    final RequestHandler requestHandler;

    public BroadcastsHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public Many<Broadcast> official(int  nb) {
        return Endpoint.officialBroadcasts.newRequest(request -> request
                .query(Map.of("nb", nb)))
            .process(requestHandler);
    }

    @Override
    public Many<Broadcast> official() {
        return Endpoint.officialBroadcasts.newRequest(request -> {})
            .process(requestHandler);
    }

    @Override
    public Many<Pgn> streamBroadcast(String roundId) {
        return Endpoint.streamBroadcast.newRequest(request -> request
                .path(roundId)
                .stream())
            .process(requestHandler);
    }

    @Override
    public Many<Pgn> exportOneRoundPgn(String roundId) {
        return Endpoint.exportBroadcastOneRoundPgn.newRequest(request -> request
                .path(roundId))
            .process(requestHandler);
    }

    @Override
    public Many<Pgn> exportPgn(String tourId) {
        return Endpoint.exportBroadcastAllRoundsPgn.newRequest(request -> request
                .path(tourId))
            .process(requestHandler);
    }

    @Override
    public One<Broadcast> broadcastById(String tourId) {
        return Endpoint.broadcastById.newRequest(request -> request
                .path(tourId))
            .process(requestHandler);
    }

    @Override
    public One<Broadcast> create(Consumer<BroadcastBuilder> params) {
        return Endpoint.createBroadcast.newRequest(request -> request
                .body(broadastBuilderToMap(params)))
            .process(requestHandler);
    }

    @Override
    public One<Void> update(String tourId, Consumer<BroadcastBuilder> params) {
        return Endpoint.updateBroadcast.newRequest(request -> request
                .path(tourId)
                .body(broadastBuilderToMap(params)))
            .process(requestHandler);
    }

    @Override
    public One<Round> roundById(String roundId) {
        return Endpoint.roundById.newRequest(request -> request
                .path(roundId))
            .process(requestHandler);
     }

    @Override
    public One<Broadcast.Round> createRound(String tourId, Consumer<RoundBuilder> params) {
        return Endpoint.createRound.newRequest(request -> request
                .path(tourId)
                .body(MapBuilder.of(RoundBuilder.class).toMap(params)))
            .process(requestHandler);
    }

    @Override
    public One<Broadcast.Round> updateRound(String roundId, Consumer<RoundBuilder> params) {
        return Endpoint.updateRound.newRequest(request -> request
                .path(roundId)
                .body(MapBuilder.of(RoundBuilder.class).toMap(params)))
            .process(requestHandler);
    }

    @Override
    public One<String> pushPgnByRoundId(String roundId, String pgn) {
        return Endpoint.pushPGNbyRoundId.newRequest(request -> request
                .path(roundId)
                .body(pgn))
            .process(requestHandler);
    }

    @Override
    public Many<BCRound> myRounds(Consumer<RoundsBuilder> params) {
        return Endpoint.streamMyRounds.newRequest(request -> request
                .query(MapBuilder.of(RoundsBuilder.class).toMap(params)))
            .process(requestHandler);
    }

    private Map<String, Object> broadastBuilderToMap(Consumer<BroadcastBuilder> consumer) {
        return MapBuilder.of(BroadcastBuilder.class)
                    .rename("shortDescription", "description")
                    .rename("longDescription", "markdown")
                    .toMap(consumer);
    }

}
