package chariot.api;

import chariot.model.*;

/**
 * Lookup positions from the Lichess tablebase server.
 */
public interface Tablebase {
    /**
     * Lookup positions from the Lichess tablebase server.
     */
    One<TablebaseResult> standard(String fen);

    /**
     * Lookup Atomic positions from the Lichess tablebase server.
     */
    One<TablebaseResult> atomic(String fen);

    /**
     * Lookup Antichess positions from the Lichess tablebase server.
     */
    One<TablebaseResult> antichess(String fen);
}
