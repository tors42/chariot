package chariot.internal.impl;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import chariot.api.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.*;
import chariot.model.Enums.Channel;
import chariot.model.PerfStat.Stat.PerfType;

public class GamesImpl extends Base implements Games {

    GamesImpl(InternalClient client) {
        super(client);
    }

    @Override
    public One<Game> byGameId(String gameId, Consumer<GameParams> params) {
        return Endpoint.gameById.newRequest(request -> request
                .path(gameId)
                .query(MapBuilder.of(GameParams.class).toMap(params)))
            .process(this);
    }

    @Override
    public One<Game> currentByUserId(String userId, Consumer<GameParams> params) {
        return Endpoint.gameCurrentByUserId.newRequest(request -> request
                .path(userId)
                .query(MapBuilder.of(GameParams.class).toMap(params)))
            .process(this);
    }

    @Override
    public Many<Game> byUserId(String userId, Consumer<SearchFilter> params) {
        return Endpoint.gamesByUserId.newRequest(request -> request
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
                    .toMap(params))
                .timeout(Duration.ofHours(1)))
            .process(this);
    }

    @Override
    public Many<Game> byGameIds(Set<String> gameIds, Consumer<GameParams> params) {
        return Endpoint.gamesByIds.newRequest(request -> request
                .post(gameIds.stream()
                    .limit(300)
                    .collect(Collectors.joining(",")))
                .query(MapBuilder.of(GameParams.class).toMap(params)))
            .process(this);
    }

    @Override
    public One<GameImport> importGame(String pgn) {
        return Endpoint.gameImport.newRequest(request -> request
            .post(Map.of("pgn", pgn)))
            .process(this);
    }

    @Override
    public Many<GameInfo> gameInfosByUserIds(Set<String> userIds, Consumer<GamesParameters> consumer) {
        var builder = MapBuilder.of(GamesParameters.class)
            .addCustomHandler("withCurrentGames", (args, map) -> {
                if (args[0] instanceof Boolean b && b.booleanValue()) map.put("withCurrentGames", 1);
            });

        return Endpoint.streamGamesByUsers.newRequest(request -> request
                .post(userIds.stream()
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
                .post(String.join(",", gameIds)))
            .process(this);
    }

    @Override
    public One<Ack> addGameIdsToStream(String streamId, Set<String> gameIds) {
        return Endpoint.addGameIdsToStream.newRequest(request -> request
                .path(streamId)
                .post(String.join(",", gameIds)))
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
        return Endpoint.gameTVFeed.newRequest(request -> {})
            .process(this);
    }

    @Override
    public Many<Game> byChannel(Channel channel, Consumer<ChannelFilter> params) {
        return Endpoint.gamesTVChannel.newRequest(request -> request
            .path(channel.name())
            .query(MapBuilder.of(ChannelFilter.class).toMap(params)))
            .process(this);
    }
 }
