package chariot.internal.impl;

import java.time.ZonedDateTime;
import java.util.List;
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
    public One<PuzzleReplay> replay(Consumer<PuzzleReplayParams> params) {
        var angleMap = MapBuilder.of(PuzzleReplayParams.class)
            .addCustomHandler("theme", (args, map) -> {
                if (args[0] instanceof PuzzleAngle angle) {
                    map.put("angle", switch(angle) {
                        case PuzzleAngle.Theme.long_ -> "long";
                        case PuzzleAngle.Theme.short_ -> "short";
                        case PuzzleAngle.Theme theme -> theme.name();
                        case PuzzleAngle.Custom(String name) -> name;
                    });
                }
            }).toMap(params);
        return Endpoint.puzzleReplay.newRequest(request -> request
                .path(angleMap.get("days"), angleMap.getOrDefault("angle", "mix")))
            .process(requestHandler);
    }


    @Override
    public One<Puzzle> nextPuzzle(Consumer<PuzzleParams> params) {
        var paramsMap = MapBuilder.of(PuzzleParams.class)
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

        return Endpoint.puzzleNext.newRequest(request -> request
                .query(paramsMap))
            .process(requestHandler);
    }

    @Override
    public Many<Puzzle> batch(Consumer<PuzzleNbParams> params) {
        var paramMap = MapBuilder.of(PuzzleNbParams.class)
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

        var angle = paramMap.getOrDefault("angle", "mix");

        return Endpoint.puzzleBatchGet.newRequest(request -> request
                .path(angle)
                .query(paramMap))
            .process(requestHandler);
    }

    @Override
    public One<PuzzleRound> batchSolve(List<PuzzleRound.Solution> solutions, Consumer<PuzzleNbSolveParams> params) {
        var paramMap = MapBuilder.of(PuzzleNbSolveParams.class)
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

        var angle = paramMap.getOrDefault("angle", "mix");

        return Endpoint.puzzleBatchSolve.newRequest(request -> request
                .path(angle)
                .query(paramMap)
                .body("""
                    {
                        "solutions": [
                            %s
                        ]
                    }
                    """.formatted(solutions.stream().map(solution -> """
                            {
                                "id": "%s",
                                "win": %s,
                                "rated": %s
                            }
                            """.formatted(solution.id(), solution.win(), solution.rated()))
                            .collect(Collectors.joining(",")))))
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

    @Override
    public One<PuzzleRaceResult> raceResult(String id) {
        return Endpoint.puzzleRaceResult.newRequest(request -> request
                .path(id))
            .process(requestHandler);
    }


}
