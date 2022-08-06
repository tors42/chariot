package chariot.internal.impl;

import java.util.function.Consumer;

import chariot.api.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.One;

public class AnalysisImpl extends Base implements Analysis {

    public AnalysisImpl(InternalClient client) {
        super(client);
    }

    @Override
    public One<chariot.model.Analysis> cloudEval(String fen, Consumer<Params> consumer) {
        return Endpoint.cloudEval.newRequest(request -> request
                .query(MapBuilder.of(Params.class).add("fen", fen).toMap(consumer)))
            .process(this);
    }

}
