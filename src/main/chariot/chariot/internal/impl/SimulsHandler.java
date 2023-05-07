package chariot.internal.impl;

import chariot.api.Simuls;
import chariot.internal.Endpoint;
import chariot.internal.RequestHandler;
import chariot.model.One;

public class SimulsHandler implements chariot.api.Simuls {
    private final RequestHandler requestHandler;

    public SimulsHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }


    @Override
    public One<chariot.model.Simuls> simuls() {
        return Endpoint.simuls.newRequest(request -> {})
            .process(requestHandler);
    }
}
