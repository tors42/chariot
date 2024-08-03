package chariot.internal.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import chariot.api.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.*;

public class BroadcastsHandler implements BroadcastsAuth {

    final RequestHandler requestHandler;

    public BroadcastsHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public Many<Broadcast> official(Consumer<BroadcastParameters> params) {
        return Endpoint.officialBroadcasts.newRequest(request -> request
                .query(MapBuilder.of(BroadcastParameters.class).toMap(params)))
            .process(requestHandler);
    }

    @Override
    public Many<Broadcast.TourWithLastRound> topActive(Consumer<Params> params) {
        return Endpoint.broadcastsTopActive.newRequest(request -> request
                .query(MapBuilder.of(Params.class).toMap(params)))
            .process(requestHandler);
    }

    @Override
    public Many<Broadcast.TourWithLastRound> topUpcoming(Consumer<Params> params) {
        return Endpoint.broadcastsTopUpcoming.newRequest(request -> request
                .query(MapBuilder.of(Params.class).toMap(params)))
            .process(requestHandler);
    }


    @Override
    public Many<Broadcast.TourWithLastRound> topPast(Consumer<Params> params) {
        var firstPage = topPastByPage(1, params);
        if (firstPage instanceof Entry<Endpoint.PageBroadcast> one) {
            var spliterator = Util.PageSpliterator.of(
                    one.entry(),
                    pageNum -> topPastByPage(pageNum, params) instanceof Entry<Endpoint.PageBroadcast> pt
                     ? pt.entry()
                     : new Endpoint.PageBroadcast(0,0,List.of(),0,0,0,0));
            return Many.entries(StreamSupport.stream(spliterator, false));
        } else {
            return Many.entries(Stream.of());
        }
    }

    private One<Endpoint.PageBroadcast> topPastByPage(int page, Consumer<Params> params) {
        return Endpoint.broadcastsTopPastPage.newRequest(request -> request
                .query(MapBuilder.of(Params.class).add("page", page).toMap(params)))
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
    public One<Broadcast> broadcastById(String tourId, Consumer<Params> params) {
        return Endpoint.broadcastById.newRequest(request -> request
                .path(tourId)
                .query(MapBuilder.of(Params.class).toMap(params)))
            .process(requestHandler);
    }

    @Override
    public Many<Broadcast.TourWithLastRound> byUserId(String userId, Consumer<Params> params) {
        var firstPage = broadcastPageByUserId(userId, 1, params);
        if (firstPage instanceof Entry<Endpoint.PageBroadcast> one) {
            var spliterator = Util.PageSpliterator.of(one.entry(),
                    pageNum -> broadcastPageByUserId(userId, pageNum, params) instanceof Entry<Endpoint.PageBroadcast> pt ?
                    pt.entry() : new Endpoint.PageBroadcast(0,0,List.of(),0,0,0,0));
            return Many.entries(StreamSupport.stream(spliterator, false));
        } else {
            return Many.entries(Stream.of());
        }
    }

    private One<Endpoint.PageBroadcast> broadcastPageByUserId(String userId, int page, Consumer<Params> params) {
        return Endpoint.broadcastPageByUser.newRequest(request -> request
                .path(userId)
                .query(MapBuilder.of(Params.class).add("page", page).toMap(params)))
            .process(requestHandler);
    }

    @Override
    public Many<LeaderboardEntry> leaderboardById(String tourId) {
        return Endpoint.broadcastLeaderboard.newRequest(request -> request
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
    public One<RoundInfo> roundById(String roundId) {
        return Endpoint.roundById.newRequest(request -> request
                .path(roundId))
            .process(requestHandler);
     }

    @Override
    public One<MyRound> createRound(String tourId, Consumer<RoundBuilder> params) {
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
    public One<Void> resetRound(String roundId) {
        return Endpoint.resetRound.newRequest(request -> request
                .path(roundId))
            .process(requestHandler);
    }

    @Override
    public Many<PushResult> pushPgnByRoundId(String roundId, String pgn) {
        return Endpoint.pushPGNbyRoundId.newRequest(request -> request
                .path(roundId)
                .body(pgn))
            .process(requestHandler);
    }

    @Override
    public Many<MyRound> myRounds(Consumer<RoundsParameters> params) {
        return Endpoint.streamMyRounds.newRequest(request -> request
                .query(MapBuilder.of(RoundsParameters.class).toMap(params)))
            .process(requestHandler);
    }

    private Map<String, Object> broadastBuilderToMap(Consumer<BroadcastBuilder> consumer) {
        return MapBuilder.of(BroadcastBuilder.class)
                    .rename("description", "markdown")
                    .rename("infoTimeControl", "info.tc")
                    .rename("infoTournamentFormat", "info.format")
                    .rename("infoFeaturedPlayers", "info.players")
                    .toMap(consumer);
    }
}
