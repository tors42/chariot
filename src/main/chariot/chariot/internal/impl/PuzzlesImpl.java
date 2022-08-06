package chariot.internal.impl;

import java.util.function.Consumer;

import chariot.api.One;
import chariot.api.Puzzles;
import chariot.internal.Base;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.internal.Util.MapBuilder;
import chariot.model.Puzzle;
import chariot.model.StormDashboard;

public class PuzzlesImpl extends Base implements Puzzles {

    PuzzlesImpl(InternalClient client) {
        super(client);
    }

    @Override
    public One<Puzzle> dailyPuzzle() {
        return Endpoint.dailyPuzzle.newRequest(request -> {})
            .process(this);
    }

    @Override
    public One<StormDashboard> stormDashboard(String username, Consumer<PuzzleParams> consumer) {
        return Endpoint.stormDashboard.newRequest(request -> request
                .path(username)
                .query(MapBuilder.of(PuzzleParams.class).toMap(consumer)))
            .process(this);
    }
}
