package chariot.internal.impl;

import java.util.Map;

import chariot.api.*;
import chariot.internal.*;
import chariot.model.*;

public class PuzzlesAuthImpl extends PuzzlesImpl implements PuzzlesAuth {

    public PuzzlesAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Many<PuzzleActivity> activity(int max) {
        return Endpoint.puzzleActivity.newRequest(request -> request
                .query(Map.of("max", max)))
            .process(this);
    }
    @Override
    public Many<PuzzleActivity> activity() {
        return Endpoint.puzzleActivity.newRequest(request -> {})
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
        return Endpoint.puzzleRace.newRequest(request -> request
                .post())
            .process(this);
    }
}
