package chariot.internal.impl;

import java.util.Map;

import chariot.api.FideApi;
import chariot.internal.*;
import chariot.model.*;

public class FideApiHandler implements FideApi {

    private final RequestHandler requestHandler;

    public FideApiHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public One<FidePlayer> byId(int fideId) {
        return Endpoint.fidePlayer.newRequest(request -> request
                .path(fideId))
            .process(requestHandler);
    }

    @Override
    public Many<FidePlayer> searchByName(String name) {
        return Endpoint.fidePlayers.newRequest(request -> request
                .query(Map.of("q", name)))
            .process(requestHandler);
    }
}
