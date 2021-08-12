package chariot.internal.impl;

import java.util.Map;
import java.util.Optional;

import chariot.internal.Base;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.model.Puzzle;
import chariot.model.Result;
import chariot.model.StormDashboard;

public class PuzzlesImpl extends Base implements Internal.Puzzles {

    PuzzlesImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<Puzzle> dailyPuzzle() {
        var request = Endpoint.dailyPuzzle.newRequest()
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<StormDashboard> stormDashboard(String username, Optional<Integer> days) {
        var requestBuilder = Endpoint.stormDashboard.newRequest()
            .path(username);
        days.ifPresent(d -> requestBuilder.query(Map.of("days", d)));
        var request = requestBuilder.build();
        return fetchOne(request);
    }

}
