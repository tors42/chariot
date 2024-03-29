package chariot.internal.impl;

import java.time.ZonedDateTime;
import java.util.function.Consumer;

import chariot.api.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.*;

public class PuzzlesHandler implements PuzzlesAuth {

    private final RequestHandler requestHandler;

    public PuzzlesHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public One<Puzzle> dailyPuzzle() {
        return Endpoint.dailyPuzzle.newRequest(request -> {})
            .process(requestHandler);
    }

    @Override
    public One<Puzzle> byId(String puzzleId) {
        return Endpoint.puzzleById.newRequest(request -> request
                .path(puzzleId))
            .process(requestHandler);
    }

    @Override
    public One<StormDashboard> stormDashboard(String username, Consumer<PuzzleParams> consumer) {
        return Endpoint.stormDashboard.newRequest(request -> request
                .path(username)
                .query(MapBuilder.of(PuzzleParams.class).toMap(consumer)))
            .process(requestHandler);
    }

    @Override
    public Many<PuzzleActivity> activity(Consumer<PuzzleActivityParams> params) {
        return Endpoint.puzzleActivity.newRequest(request -> request
                .query(
                    MapBuilder.of(PuzzleActivityParams.class)
                    .addCustomHandler("before", (args, map) -> map.put("before",
                            ((ZonedDateTime) args[0]).toInstant().toEpochMilli()))
                    .addCustomHandler("since",  (args, map) -> map.put("since",
                            ((ZonedDateTime) args[0]).toInstant().toEpochMilli()))
                    .toMap(params))
                )
            .process(requestHandler);
    }


    @Override
    public One<PuzzleDashboard> puzzleDashboard(int days) {
        return Endpoint.puzzleDashboard.newRequest(request -> request
                .path(String.valueOf(days)))
            .process(requestHandler);
    }

    @Override
    public One<PuzzleRace> createAndJoinRace() {
        return Endpoint.puzzleRace.newRequest(request -> {})
            .process(requestHandler);
    }
}
