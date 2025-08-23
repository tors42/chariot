package chariot.api;

import java.util.function.Consumer;

import chariot.model.*;

public interface GamesApiAuth extends GamesApi {
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
    Many<PGN> imported();

    Many<Game> bookmarked(Consumer<BookmarkedParams> params);
    Many<PGN> pgnBookmarked(Consumer<BookmarkedParams> params);

    default Many<Game> bookmarked() { return bookmarked(_ -> {}); }
    default Many<PGN> pgnBookmarked() { return pgnBookmarked(_ -> {}); }

    /// Download games of a bulk
    Many<Game> byBulkId(String bulkId, Consumer<GameParams> params);
    /// Download games of a bulk
    default Many<Game> byBulkId(String bulkId) { return byBulkId(bulkId, _ -> {}); }

    /// Download games of a bulk
    Many<PGN> pgnByBulkId(String bulkId, Consumer<GameParams> params);
    /// Download games of a bulk
    default Many<PGN> pgnByBulkId(String bulkId) { return pgnByBulkId(bulkId, _ -> {}); }

    interface BookmarkedParams extends CommonSearchFilterParams<BookmarkedParams>, EvalsDefaultFalse<BookmarkedParams> {}

}
