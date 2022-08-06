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
import chariot.model.Enums.Speed;
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
    public One<Pgn> openingExplorerMastersOTB(String gameId) {
        return Endpoint.exploreMasterOTB.newRequest(request -> request
            .path(gameId))
            .process(this);
    }

    @Override
    public One<ExploreResult.OpeningDB> openingExplorerMasters(Consumer<MastersBuilder> params) {
        return Endpoint.exploreMasters.newRequest(request -> request
            .query(MapBuilder.of(MastersBuilder.class).toMap(params)))
            .process(this);
    }

    @Override
    public One<ExploreResult.OpeningDB> openingExplorerLichess(Consumer<LichessBuilder> params) {
        return Endpoint.exploreLichess.newRequest(request -> request
            .query(MapBuilder.of(LichessBuilder.class)
               .addCustomHandler("speeds", (args, map) -> {
                    @SuppressWarnings("unchecked")
                    var speeds = (Set<Speed>) args[0];
                    if (! speeds.isEmpty()) {
                        map.put("speeds", speeds.stream().map(Speed::name).collect(Collectors.joining(",")));
                    }
                })
               .addCustomHandler("ratings", (args, map) -> {
                    @SuppressWarnings("unchecked")
                    var ratings = (Set<LichessBuilder.RatingGroup>) args[0];
                    if (! ratings.isEmpty()) {
                        map.put("ratings", ratings.stream().map(LichessBuilder.RatingGroup::asString).collect(Collectors.joining(",")));
                    }
                }).toMap(params)))
            .process(this);
     }

    @Override
    public One<ExploreResult.OpeningPlayer> openingExplorerPlayer(String userId, Consumer<PlayerBuilder> params) {
        return Endpoint.explorePlayers.newRequest(request -> request
            .query(MapBuilder.of(PlayerBuilder.class)
                .add("player", userId)
                .add("color", chariot.model.Enums.Color.white)
                .addCustomHandler("speeds", (args, map) -> {
                    @SuppressWarnings("unchecked")
                    var speeds = (Set<Speed>) args[0];
                    if (! speeds.isEmpty()) {
                        map.put("speeds", speeds.stream().map(Speed::name).collect(Collectors.joining(",")));
                    }
                })
                .addCustomHandler("ratings", (args, map) -> {
                    @SuppressWarnings("unchecked")
                    var modes = (Set<Games.PlayerBuilder.Mode>) args[0];
                    if (! modes.isEmpty()) {
                        map.put("modes", modes.stream().map(Games.PlayerBuilder.Mode::name).collect(Collectors.joining(",")));
                    }
                })
                .toMap(params)))
            .process(this);
     }

    @Override
    public One<TablebaseResult> lookupTablebase(String fen) {
        return Endpoint.tablebaseLookup.newRequest(request -> request
                .query(Map.of("fen", fen)))
            .process(this);
    }

    @Override
    public One<TablebaseResult> lookupTablebaseAtomic(String fen) {
        return Endpoint.tablebaseAtomicLookup.newRequest(request -> request
            .query(Map.of("fen", fen)))
            .process(this);
    }

    @Override
    public One<TablebaseResult> lookupTablebaseAntichess(String fen) {
        return Endpoint.tablebaseAntichessLookup.newRequest(request -> request
            .query(Map.of("fen", fen)))
            .process(this);
    }

    @Override
    public Many<StreamGame> streamGamesByUserIds(boolean withCurrentGames, Set<String> userIds) {
        Consumer<Params> params = request -> request
            .post(userIds.stream()
                    .limit(300)
                    .collect(Collectors.joining(",")))
            .stream();

        if (withCurrentGames) {
            params = params.andThen(request -> request.query(Map.of("withCurrentGames", 1)));
        }

        return Endpoint.streamGamesByUsers.newRequest(params)
            .process(this);
     }

    @Override
    public Many<StreamMove> streamMovesByGameId(String gameId) {
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
    public Many<TVFeed> tvFeed() {
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
