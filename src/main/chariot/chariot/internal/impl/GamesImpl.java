package chariot.internal.impl;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import chariot.internal.*;
import chariot.model.*;
import chariot.model.Enums.Channel;

public class GamesImpl extends Base implements Internal.Games {

    GamesImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<Game> byGameId(String gameId, InternalGameParams params) {
        var request = Endpoint.gameById.newRequest()
            .path(gameId)
            .query(params.toMap())
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<Game> byUserId(String userId, InternalSearchFilter gamesFilter) {
        var request = Endpoint.gamesByUserId.newRequest()
            .path(userId)
            .query(gamesFilter.toMap())
            .timeout(Duration.ofHours(1))
            .build();
        return fetchMany(request);
    }

    @Override
    public Result<Game> currentByUserId(String userId, InternalGameParams params) {
        var request = Endpoint.gameCurrentByUserId.newRequest()
            .path(userId)
            .query(params.toMap())
            .build();
        return fetchOne(request);
    }

    public Result<Game> byGameIds(Set<String> gameIds, InternalGameParams gameParameters) {
        var ids = gameIds.stream()
            .limit(300)
            .collect(Collectors.joining(","));

        var request = Endpoint.gamesByIds.newRequest()
            .post(ids)
            .query(gameParameters.toMap())
            .build();

        return fetchMany(request);
    }

    @Override
    public Result<StreamGame> streamGamesByUserIds(boolean withCurrentGames, Set<String> userIds) {
        var ids = userIds.stream()
            .limit(300)
            .collect(Collectors.joining(","));

        var builder = Endpoint.streamGamesByUsers.newRequest();

        if (withCurrentGames) builder.query(Map.of("withCurrentGames", 1));

        var request = builder
            .post(ids)
            .stream()
            .build();

        return fetchMany(request);
     }

    @Override
    public Result<StreamMove> streamMovesByGameId(String gameId) {
        var request = Endpoint.streamMoves.newRequest()
            .path(gameId)
            .stream()
            .build();

        return fetchMany(request);
    }


    @Override
    public Result<TVChannels> tvChannels() {
        var request = Endpoint.gameTVChannels.newRequest()
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<TVFeed> tvFeed() {
        var request = Endpoint.gameTVFeed.newRequest()
            .build();
        return fetchMany(request);
    }

    @Override
    public Result<Game> byChannel(Channel channel, ChannelParameters channelParameters) {
        var request = Endpoint.gamesTVChannel.newRequest()
            .path(channel.name())
            .query(channelParameters.toMap())
            .build();
        return fetchMany(request);
    }

    @Override
    public Result<GameImport> importGame(String pgn) {
        var request = Endpoint.gameImport.newRequest()
            .post("pgn=" + pgn)
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<ExploreResult.OpeningDB> openingExplorerMasters(MastersParameters mastersParameters) {
        var request = Endpoint.exploreMasters.newRequest()
            .query(mastersParameters.toMap())
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<ExploreResult.OpeningDB> openingExplorerLichess(LichessParameters lichessParameters) {
        var request = Endpoint.exploreLichess.newRequest()
            .query(lichessParameters.toMap())
            .build();

        return fetchOne(request);
     }

    @Override
    public Result<ExploreResult.OpeningPlayer> openingExplorerPlayer(PlayerParameters playerParameters) {
        var request = Endpoint.explorePlayers.newRequest()
            .query(playerParameters.toMap())
            .build();

        return fetchOne(request);
     }


    @Override
    public Result<Pgn> openingExplorerMastersOTB(String gameId) {
        var request = Endpoint.exploreMasterOTB.newRequest()
            .path(gameId)
            .errorMapper(e -> Err.fail(e)) // Not json, so the default ModelMapper-parser would fail
            .build();

        var result = fetchMany(request);
        return Util.toPgnResult(result);
    }

    @Override
    public Result<TablebaseResult> lookupTablebase(String fen) {
        var request = Endpoint.tablebaseLookup.newRequest()
            .query(Map.of("fen", fen))
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<TablebaseResult> lookupTablebaseAtomic(String fen) {
        var request = Endpoint.tablebaseAtomicLookup.newRequest()
            .query(Map.of("fen", fen))
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<TablebaseResult> lookupTablebaseAntichess(String fen) {
        var request = Endpoint.tablebaseAntichessLookup.newRequest()
            .query(Map.of("fen", fen))
            .build();

        return fetchOne(request);
    }

 }
