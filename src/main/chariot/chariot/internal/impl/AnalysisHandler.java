package chariot.internal.impl;

import java.util.function.Consumer;

import chariot.api.Analysis;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.*;

public class AnalysisHandler implements Analysis {

    private final RequestHandler requestHandler;

    public AnalysisHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public One<CloudEvalCacheEntry> cloudEval(String fen, Consumer<Params> consumer) {
        return Endpoint.cloudEval.newRequest(request -> request
                .query(MapBuilder.of(Params.class).add("fen", fen).toMap(consumer)))
            .process(requestHandler);
    }

}
