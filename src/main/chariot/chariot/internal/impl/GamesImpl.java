package chariot.internal.impl;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import chariot.api.*;
import chariot.internal.*;
import chariot.internal.RequestParameters.Params;
import chariot.internal.Util.MapBuilder;
import chariot.model.*;
import chariot.model.Enums.Channel;
import chariot.model.Enums.PerfType;

public class GamesImpl extends Base implements Games {

    GamesImpl(InternalClient client) {
        super(client);
    }

    @Override
    public One<Game> byGameId(String gameId, Consumer<GameParams> params) {
        return Endpoint.gameById.newRequest(paramsConsumerByIdGameParams(gameId, params))
            .process(this);
    }

    @Override
    public One<Pgn> pgnByGameId(String gameId, Consumer<GameParams> params) {
        return Endpoint.gameByIdPgn.newRequest(paramsConsumerByIdGameParams(gameId, params))
            .process(this);
    }

    @Override
    public One<Game> currentByUserId(String userId, Consumer<GameParams> params) {
        return Endpoint.gameCurrentByUserId.newRequest(paramsConsumerByIdGameParams(userId, params))
            .process(this);
    }

    @Override
    public One<Pgn> pgnCurrentByUserId(String userId, Consumer<GameParams> params) {
        return Endpoint.gameCurrentByUserIdPgn.newRequest(paramsConsumerByIdGameParams(userId, params))
            .process(this);
    }

    @Override
    public Many<Game> byUserId(String userId, Consumer<SearchFilter> params) {
        return Endpoint.gamesByUserId.newRequest(paramsConsumerByUserId(userId, params))
            .process(this);
    }

    @Override
    public Many<Pgn> pgnByUserId(String userId, Consumer<SearchFilter> params) {
        return Endpoint.gamesByUserIdPgn.newRequest(paramsConsumerByUserId(userId, params))
            .process(this);
    }

    @Override
    public Many<Game> byGameIds(Set<String> gameIds, Consumer<GameParams> params) {
        return Endpoint.gamesByIds.newRequest(paramsConsumerByIdsGameParams(gameIds, params))
            .process(this);
    }

    @Override
    public Many<Pgn> pgnByGameIds(Set<String> gameIds, Consumer<GameParams> params) {
        return Endpoint.gamesByIdsPgn.newRequest(paramsConsumerByIdsGameParams(gameIds, params))
            .process(this);
    }

    @Override
    public One<GameImport> importGame(String pgn) {
        return Endpoint.gameImport.newRequest(request -> request
            .body(Map.of("pgn", pgn)))
            .process(this);
    }

    @Override
    public Many<GameInfo> gameInfosByUserIds(Set<String> userIds, Consumer<GamesParameters> consumer) {
        var builder = MapBuilder.of(GamesParameters.class)
            .addCustomHandler("withCurrentGames", (args, map) -> {
                if (args[0] instanceof Boolean b && b.booleanValue()) map.put("withCurrentGames", 1);
            });

        return Endpoint.streamGamesByUsers.newRequest(request -> request
                .body(userIds.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.joining(",")))
                .query(builder.toMap(consumer))
                .stream())
            .process(this);
    }

    @Override
    public Many<GameInfo> gameInfosByGameIds(String streamId, Set<String> gameIds) {
        return Endpoint.streamGamesByStreamIds.newRequest(request -> request
                .path(streamId)
                .body(String.join(",", gameIds))
                .stream())
            .process(this);
    }

    @Override
    public One<Ack> addGameIdsToStream(String streamId, Set<String> gameIds) {
        return Endpoint.addGameIdsToStream.newRequest(request -> request
                .path(streamId)
                .body(String.join(",", gameIds)))
            .process(this);
    }

    @Override
    public Many<MoveInfo> moveInfosByGameId(String gameId) {
        return Endpoint.streamMoves.newRequest(request -> request
            .path(gameId)
            .stream())
            .process(this);
    }

    @Override
    public One<TVChannels> tvChannels() {
        return Endpoint.gameTVChannels.newRequest(request -> {})
            .process(this);
    }

    @Override
    public Many<TVFeedEvent> tvFeed() {
        return Endpoint.gameTVFeed.newRequest(request -> request.stream())
            .process(this);
    }

    @Override
    public Many<Game> byChannel(Channel channel, Consumer<ChannelFilter> params) {
        return Endpoint.gamesTVChannel.newRequest(paramsConsumerByChannelChannelFilter(channel, params))
            .process(this);
    }

    @Override
    public Many<Pgn> pgnByChannel(Channel channel, Consumer<ChannelFilter> params) {
        return Endpoint.gamesTVChannelPgn.newRequest(paramsConsumerByChannelChannelFilter(channel, params))
            .process(this);
    }

    static Consumer<Params> paramsConsumerByChannelChannelFilter(Channel channel, Consumer<ChannelFilter> params) {
        return request -> request
            .path(channel.name())
            .query(channelFilterBuilder().toMap(params));
    }



    static MapBuilder<GameParams> gameParamsBuilder() { return builder(GameParams.class); }
    static MapBuilder<Filter> filterBuilder() { return builder(Filter.class); }
    static MapBuilder<ChannelFilter> channelFilterBuilder() { return builder(ChannelFilter.class); }

    static <T extends CommonGameParameters<?>> MapBuilder<T> builder(Class<T> clazz) {
        return MapBuilder.of(clazz).rename("pgn", "pgnInJson");
    }


    static Consumer<Params> paramsConsumerByUserId(String userId, Consumer<SearchFilter> params) {
        return request -> request
                .path(userId)
                .query(MapBuilder.of(SearchFilter.class)
                    .addCustomHandler("perfType", (args, map) ->
                        map.put("perfType",
                            Arrays.stream((PerfType[]) args[0])
                            .map(PerfType::name)
                            .collect(Collectors.joining(",")))
                        )
                    .addCustomHandler("sortAscending", (args, map) -> {
                        map.put("sort", (boolean) args[0] ? "dateAsc" : "dateDesc");
                    })
                    .rename("pgn", "pgnInJson")
                    .toMap(params))
                .timeout(Duration.ofHours(1));
    }

    static Consumer<Params> paramsConsumerByIdGameParams(String pathId, Consumer<GameParams> params) {
        return request -> request
            .path(pathId)
            .query(gameParamsBuilder().toMap(params));
    }

    static Consumer<Params> paramsConsumerByIdsGameParams(Set<String> gameIds, Consumer<GameParams> params) {
        return request -> request
            .body(gameIds.stream()
                    .limit(300)
                    .collect(Collectors.joining(",")))
            .query(gameParamsBuilder().toMap(params));
    }

}
