package chariot.internal.impl;

import module java.base;
import module chariot;

import chariot.internal.*;
import chariot.internal.Util.MapBuilder;

public class BroadcastsHandler implements BroadcastsApiAuth {

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
    public Many<Broadcast.TourWithLastRound> topPast(Consumer<Params> params) {
        var firstPage = topPastByPage(1, params);
        if (firstPage instanceof Some<Endpoint.PageBroadcast> one) {
            var spliterator = Util.PageSpliterator.of(
                    one.value(),
                    pageNum -> topPastByPage(pageNum, params) instanceof Some<Endpoint.PageBroadcast> pt
                     ? pt.value()
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
    public Many<BroadcastPlayer> playersById(String broadcastId) {
        return Endpoint.broadcastPlayers.newRequest(request -> request
                .path(broadcastId))
            .process(requestHandler);
    }

    @Override
    public One<BroadcastPlayer> playerDetailsById(String broadcastId, String playerId) {
        return Endpoint.broadcastPlayerById.newRequest(request -> request
                .path(broadcastId, playerId))
            .process(requestHandler);
    }

    @Override
    public Many<Broadcast.TourWithLastRound> search(String searchTerm) {
        var firstPage = searchByPage(1, searchTerm);
        if (firstPage instanceof Some<Endpoint.PageBroadcast> one) {
            var spliterator = Util.PageSpliterator.of(
                    one.value(),
                    pageNum -> searchByPage(pageNum, searchTerm) instanceof Some<Endpoint.PageBroadcast> pt
                     ? pt.value()
                     : new Endpoint.PageBroadcast(0,0,List.of(),0,0,0,0));
            return Many.entries(StreamSupport.stream(spliterator, false));
        } else {
            return Many.entries(Stream.of());
        }
    }

    private One<Endpoint.PageBroadcast> searchByPage(int page, String searchTerm) {
        return Endpoint.broadcastsSearchPage.newRequest(request -> request
                .query(Map.of("page", page, "q", searchTerm)))
            .process(requestHandler);
    }


    @Override
    public Many<PGN> streamBroadcast(String roundId) {
        return Endpoint.streamBroadcast.newRequest(request -> request
                .path(roundId)
                .stream())
            .process(requestHandler);
    }

    @Override
    public Many<PGN> exportOneRoundPgn(String roundId) {
        return Endpoint.exportBroadcastOneRoundPgn.newRequest(request -> request
                .path(roundId))
            .process(requestHandler);
    }

    @Override
    public Many<PGN> exportPgn(String tourId) {
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
        if (firstPage instanceof Some<Endpoint.PageBroadcast> one) {
            var spliterator = Util.PageSpliterator.of(one.value(),
                    pageNum -> broadcastPageByUserId(userId, pageNum, params) instanceof Some<Endpoint.PageBroadcast> pt ?
                    pt.value() : new Endpoint.PageBroadcast(0,0,List.of(),0,0,0,0));
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
    public One<Broadcast> create(Consumer<BroadcastBuilder> params) {
        return Endpoint.createBroadcast.newRequest(request -> request
                .body(broadastBuilderToMap(params)))
            .process(requestHandler);
    }

    @Override
    public Ack update(String tourId, Consumer<BroadcastBuilder> params) {
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
    public Many<TeamLeaderboardEntry> teamLeaderboard(String broadcastId) {
        return Endpoint.broadcastTeamsStandings.newRequest(request -> request
                .path(broadcastId))
            .process(requestHandler);
    }

    @Override
    public One<MyRound> createRound(String tourId, Consumer<RoundBuilder> params) {
        var map = roundBuilderToMap(params);
        map.remove("patch");
        return Endpoint.createRound.newRequest(request -> request
                .path(tourId)
                .body(map))
            .process(requestHandler);
    }

    @Override
    public One<Broadcast.Round> updateRound(String roundId, Consumer<RoundBuilder> params) {
        var map = roundBuilderToMap(params);
        String patch = Objects.toString(map.remove("patch"), "true");
        return Endpoint.updateRound.newRequest(request -> request
                .path(roundId)
                .query(Map.of("patch", patch))
                .body(map))
            .process(requestHandler);
    }

    @Override
    public Ack resetRound(String roundId) {
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
                    .rename("infoTimeControlFIDE", "info.fideTC")
                    .rename("infoLocation", "info.location")
                    .rename("infoStandings", "info.standings")
                    .rename("infoWebsite", "info.website")
                    .rename("infoTimeZone", "info.timeZone")
                    .addCustomHandler("tiebreaks", (args, map) -> {
                        if (args == null || args[0] == null) return;
                        String[] arr = (String[]) args[0];
                        for (int i = 0; i < arr.length; i++) {
                            map.put("tiebreaks[%d]".formatted(i), arr[i]);
                        }
                    })
                    .toMap(consumer);
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> roundBuilderToMap(Consumer<RoundBuilder> consumer) {
        return MapBuilder.of(RoundBuilder.class)
            .addCustomHandler("syncUrls", (args, map) -> map.put("syncUrls", ((List<?>) args[0]).stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining("\n"))))
            .addCustomHandler("syncIds", (args, map) -> map.put("syncIds", String.join(" ", (List<String>)args[0])))
            .addCustomHandler("syncUsers", (args, map) -> map.put("syncUsers", String.join(" ", (List<String>)args[0])))
            .addCustomHandler("patch", (args, map) -> map.put("patch", String.valueOf(args[0])))
            .rename("customScoringWhiteWin",  "customScoring.white.win")
            .rename("customScoringBlackWin",  "customScoring.black.win")
            .rename("customScoringWhiteDraw", "customScoring.white.draw")
            .rename("customScoringBlackDraw", "customScoring.black.draw")
            .toMap(consumer);
    }
}
