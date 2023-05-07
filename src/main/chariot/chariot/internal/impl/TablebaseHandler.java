package chariot.internal.impl;

import java.util.Map;

import chariot.api.Tablebase;
import chariot.internal.*;
import chariot.model.*;

public class TablebaseHandler implements Tablebase {

    private final RequestHandler requestHandler;

    public TablebaseHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public One<TablebaseResult> standard(String fen) {
        return Endpoint.tablebaseLookup.newRequest(request -> request
                .query(Map.of("fen", fen)))
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
}
