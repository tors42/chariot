package chariot.internal.impl;

import chariot.api.FideApi;
import chariot.internal.*;
import chariot.model.*;

public class FideApiHandler implements FideApi {

    private final RequestHandler requestHandler;

    public FideApiHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public One<FidePlayer> playerById(int fideId) {
        return Endpoint.fidePlayer.newRequest(request -> request
                .path(fideId))
            .process(requestHandler);
    }
}
