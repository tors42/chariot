package chariot.internal.impl;

import chariot.api.SimulsApi;
import chariot.internal.Endpoint;
import chariot.internal.RequestHandler;
import chariot.model.*;

public class SimulsHandler implements SimulsApi {
    private final RequestHandler requestHandler;

    public SimulsHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }


    @Override
    public One<CurrentSimuls> simuls() {
        return Endpoint.simuls.newRequest(request -> {})
            .process(requestHandler);
    }
}
