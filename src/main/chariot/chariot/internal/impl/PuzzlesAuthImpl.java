package chariot.internal.impl;

import java.util.*;

import chariot.internal.*;
import chariot.model.*;

public class PuzzlesAuthImpl extends PuzzlesImpl implements Internal.PuzzlesAuth {

    public PuzzlesAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<PuzzleActivity> activity(Optional<Integer> max) {
        var requestBuilder = Endpoint.puzzleActivity.newRequest();
        max.ifPresent(m -> requestBuilder.query(Map.of("max", m)));
        var request = requestBuilder.build();
        return fetchMany(request);
    }

    @Override
    public Result<PuzzleDashboard> puzzleDashboard(int days) {
        var request = Endpoint.puzzleDashboard.newRequest()
            .path(String.valueOf(days))
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<PuzzleRace> createAndJoinRace() {
        var request = Endpoint.puzzleRace.newRequest()
            .post()
            .build();
        return fetchOne(request);
    }

}
