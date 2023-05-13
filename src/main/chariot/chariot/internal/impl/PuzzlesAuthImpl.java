package chariot.internal.impl;

import java.time.ZonedDateTime;
import java.util.function.Consumer;

import chariot.api.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.*;

public class PuzzlesAuthImpl extends PuzzlesImpl implements PuzzlesAuth {

    public PuzzlesAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Many<PuzzleActivity> activity(Consumer<PuzzleActivityParams> params) {
        return Endpoint.puzzleActivity.newRequest(request -> request
                .query(
                    MapBuilder.of(PuzzleActivityParams.class)
                    .addCustomHandler("before", (args, map) -> {
                        var zdt = (ZonedDateTime) args[0];
                        map.put("before", zdt.toInstant().toEpochMilli());
                    }).toMap(params))
                )
            .process(this);
    }

    @Override
    public One<PuzzleDashboard> puzzleDashboard(int days) {
        return Endpoint.puzzleDashboard.newRequest(request -> request
                .path(String.valueOf(days)))
            .process(this);
    }

    @Override
    public One<PuzzleRace> createAndJoinRace() {
        return Endpoint.puzzleRace.newRequest(request -> {})
            .process(this);
    }
}
