package chariot.internal.impl;

import java.util.Map;
import java.util.Optional;

import chariot.model.PuzzleDashboard;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.model.Result;
import chariot.model.PuzzleActivity;

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

}
