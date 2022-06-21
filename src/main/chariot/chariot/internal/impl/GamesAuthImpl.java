package chariot.internal.impl;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.model.GameImport;
import chariot.model.GameInfo;
import chariot.model.PlayingWrapper;
import chariot.model.Result;

public class GamesAuthImpl extends GamesImpl implements Internal.GamesAuth {

    public GamesAuthImpl(InternalClient client) {
        super(client);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Result<chariot.model.NowPlaying> ongoingGames(Optional<Integer> nb) {
        var requestBuilder = Endpoint.accountNowPlayingDeprecated.newRequest();
        nb.ifPresent(v -> requestBuilder.query(Map.of("nb", v)));
        var request = requestBuilder.build();
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
    public Result<GameInfo> ongoing() {
        var request = Endpoint.accountNowPlaying.newRequest().build();
        var result = fetchOne(request);
        System.out.println(result);
        if (result instanceof Result.One<PlayingWrapper> wrapper) {
            return Result.many(wrapper.get().nowPlaying().stream());
        }
        return Result.many(Stream.of());
    }

    @Override
    public Result<GameInfo> ongoing(int nb) {
        var request = Endpoint.accountNowPlaying.newRequest().query(Map.of("nb", nb)).build();
        var result = fetchOne(request);
        if (result instanceof Result.One<PlayingWrapper> wrapper) {
            return Result.many(wrapper.get().nowPlaying().stream());
        }
        return Result.many(Stream.of());
     }
}

