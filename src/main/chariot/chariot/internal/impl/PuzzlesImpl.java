package chariot.internal.impl;

import java.util.function.Consumer;

import chariot.api.Puzzles;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.*;

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
    public One<Puzzle> byId(String puzzleId) {
        return Endpoint.puzzleById.newRequest(request -> request
                .path(puzzleId))
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
