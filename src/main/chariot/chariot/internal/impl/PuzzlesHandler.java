package chariot.internal.impl;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import chariot.api.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.*;

public class PuzzlesHandler implements PuzzlesApiAuth {

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
    public One<Puzzle> nextPuzzle(Consumer<PuzzleParams> params, Consumer<PuzzleDifficulty> difficulty) {
        var angleMap = MapBuilder.of(PuzzleParams.class)
            .addCustomHandler("theme", (args, map) -> {
                if (args[0] instanceof PuzzleAngle angle) {
                    map.put("angle", switch(angle) {
                        case PuzzleAngle.Theme.long_ -> "long";
                        case PuzzleAngle.Theme.short_ -> "short";
                        case PuzzleAngle.Theme theme -> theme.name();
                        case PuzzleAngle.Custom(String name) -> name;
                    });
                }
            })
            .toMap(params);

        var difficultyMap = MapBuilder.of(PuzzleDifficulty.class).toMap(difficulty);

        var combined = Stream.of(angleMap, difficultyMap)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return Endpoint.puzzleNext.newRequest(request -> request
                .query(combined))
            .process(requestHandler);
 }

    @Override
    public One<StormDashboard> stormDashboard(String username, Consumer<StormDashboardParams> consumer) {
        return Endpoint.stormDashboard.newRequest(request -> request
                .path(username)
                .query(MapBuilder.of(StormDashboardParams.class).toMap(consumer)))
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
