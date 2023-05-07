package chariot.internal.impl;

import java.util.function.Consumer;

import chariot.api.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.*;

public class AnalysisHandler implements chariot.api.Analysis {

    private final RequestHandler requestHandler;

    public AnalysisHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public One<chariot.model.Analysis> cloudEval(String fen, Consumer<Params> consumer) {
        return Endpoint.cloudEval.newRequest(request -> request
                .query(MapBuilder.of(Params.class).add("fen", fen).toMap(consumer)))
            .process(requestHandler);
    }

}
