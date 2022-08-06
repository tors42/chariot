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
     * Import a game from PGN.<br>
     * Rate limiting: 200 games per hour for OAuth requests.<br>
     * See {@link Games#importGame} for non-authenticated access.<br>
     * To broadcast ongoing games, consider pushing to a broadcast instead. See {@link BroadcastsAuth#pushPgnByRoundId}
     */
    One<GameImport> importGame(String pgn);
}
