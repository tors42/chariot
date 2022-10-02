package chariot.internal.impl;

import java.util.Map;

import chariot.api.Tablebase;
import chariot.internal.*;
import chariot.model.*;

public class TablebaseImpl extends Base implements Tablebase {

    public TablebaseImpl(InternalClient client) {
        super(client);
    }

    @Override
    public One<TablebaseResult> standard(String fen) {
        return Endpoint.tablebaseLookup.newRequest(request -> request
                .query(Map.of("fen", fen)))
                .process(this);
    }

    @Override
    public One<TablebaseResult> atomic(String fen) {
        return Endpoint.tablebaseAtomicLookup.newRequest(request -> request
                .query(Map.of("fen", fen)))
                .process(this);
    }

    @Override
    public One<TablebaseResult> antichess(String fen) {
        return Endpoint.tablebaseAntichessLookup.newRequest(request -> request
                .query(Map.of("fen", fen)))
                .process(this);
    }
}
