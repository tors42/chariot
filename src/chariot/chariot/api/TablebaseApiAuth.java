package chariot.api;

import module java.base;
import module chariot;

/// Lookup positions from the Lichess tablebase server.
public interface TablebaseApiAuth {

    /// Lookup positions from the Lichess tablebase server.
    One<TablebaseResult> standard(String fen, Consumer<Params> params);
    /// Lookup positions from the Lichess tablebase server.
    default One<TablebaseResult> standard(String fen) { return standard(fen, _ -> {}); }

    /// Lookup Atomic positions from the Lichess tablebase server.
    One<TablebaseResult> atomic(String fen);

    /// Lookup Antichess positions from the Lichess tablebase server.
    One<TablebaseResult> antichess(String fen);

    interface Params {
        Params op1Never();
        Params op1Auxiliary();
        Params op1Always();
    }
}
