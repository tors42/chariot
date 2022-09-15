package chariot.internal.impl;

import java.util.Map;

import chariot.api.*;
import chariot.internal.*;
import chariot.model.*;

public class GamesAuthImpl extends GamesImpl implements GamesAuth {

    public GamesAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Many<GameInfo> ongoing() {
        return Endpoint.accountNowPlaying.newRequest(request -> {})
            .process(this);
    }

    @Override
    public Many<GameInfo> ongoing(int nb) {
        return Endpoint.accountNowPlaying.newRequest(request -> request
                .query(Map.of("nb", nb)))
            .process(this);
    }
}
