package chariot.internal.impl;

import module java.base;
import module chariot;

import chariot.internal.Endpoint;
import chariot.internal.RequestHandler;
import chariot.internal.Util.MapBuilder;

public class TablebaseHandler implements TablebaseApi {

    private final RequestHandler requestHandler;

    public TablebaseHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public One<TablebaseResult> standard(String fen, Consumer<Params> params) {
        var map = paramsToMap(params);
        map.put("fen", fen);
        return Endpoint.tablebaseLookup.newRequest(request -> request
                .query(map))
                .process(requestHandler);
    }

    @Override
    public One<TablebaseResult> atomic(String fen) {
        return Endpoint.tablebaseAtomicLookup.newRequest(request -> request
                .query(Map.of("fen", fen)))
                .process(requestHandler);
    }

    @Override
    public One<TablebaseResult> antichess(String fen) {
        return Endpoint.tablebaseAntichessLookup.newRequest(request -> request
                .query(Map.of("fen", fen)))
                .process(requestHandler);
    }

    Map<String, Object> paramsToMap(Consumer<Params> consumer) {
        return MapBuilder.of(Params.class)
            .addCustomHandler("op1Never", (_, map) -> map.put("op1", "never"))
            .addCustomHandler("op1Auxiliary", (_, map) -> map.put("op1", "auxiliary"))
            .addCustomHandler("op1Always", (_, map) -> map.put("op1", "always"))
            .toMap(consumer);
    }
}
