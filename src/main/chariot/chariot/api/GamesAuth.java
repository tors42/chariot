package chariot.api;

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
}
