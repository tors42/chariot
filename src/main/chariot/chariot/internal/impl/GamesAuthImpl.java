package chariot.internal.impl;

import java.util.Map;
import java.util.Optional;

import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.model.GameImport;
import chariot.model.NowPlaying;
import chariot.model.Result;

public class GamesAuthImpl extends GamesImpl implements Internal.GamesAuth {

    public GamesAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<NowPlaying> ongoingGames(Optional<Integer> nb) {
        var requestBuilder = Endpoint.accountNowPlaying.newRequest();
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

}

