package chariot.api;

import java.util.function.Consumer;

import chariot.model.*;

public interface GamesAuth extends Games {
    /**
     * Get the ongoing games of the current user. Real-time and correspondence games are included. The most urgent games are listed first.
     * @param nb Max number of games to fetch. [ 1 .. 50 ] Default: 9
     */
    Many<GameInfo> ongoing(int nb);
    /**
     * {@link #ongoing(int)}
     */
    Many<GameInfo> ongoing();

    /**
     * Download all games imported by you.
     */
    Many<Pgn> imported();

    /// Download games of a bulk
    Many<Game> byBulkId(String bulkId, Consumer<GameParams> params);
    /// Download games of a bulk
    default Many<Game> byBulkId(String bulkId) { return byBulkId(bulkId, __ -> {}); }

    /// Download games of a bulk
    Many<Pgn> pgnByBulkId(String bulkId, Consumer<GameParams> params);
    /// Download games of a bulk
    default Many<Pgn> pgnByBulkId(String bulkId) { return pgnByBulkId(bulkId, __ -> {}); }
}
