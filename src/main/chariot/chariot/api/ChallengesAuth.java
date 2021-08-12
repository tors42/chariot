package chariot.api;

import java.time.ZonedDateTime;
import java.util.function.Function;
import java.util.function.Supplier;

import chariot.model.Enums.*;
import chariot.model.Ack;
import chariot.model.BulkPairing;
import chariot.model.PendingChallenges;
import chariot.model.Result;

/**
 * Send and receive challenges and manage bulk challenges.
 */
public interface ChallengesAuth extends Challenges, ChallengesAuthCommon {

    Result<PendingChallenges> challenges();
    Result<Ack>               startClocksOfGame(String gameId, String token1, String token2);
    Result<Ack>               addTimeToGame(String gameId, int seconds);

    /**
     * Get a list of upcoming bulk pairings you created.<br>
     * Only bulk pairings that are scheduled in the future, or that have a clock start scheduled in the future, are listed.<br>
     * Bulk pairings are deleted from the server after the pairings are done and the clocks have started.
     *
     */
    Result<BulkPairing> bulks();

    /**
     * Schedule many games at once, up to 24h in advance.<br>
     * OAuth tokens are required for all paired players, with the
     * challenge:write scope.<br>
     * You can schedule up to 500 games every 10 minutes. Contact Lichess if
     * you need higher limits.<br>
     * The entire bulk is rejected if:
     * <ul>
     * <li> a token is missing
     * <li> a token is present more than once
     * <li> a token lacks the challenge:write scope
     * <li> a player account is closed
     * <li> a player is paired more than once
     * <li> a bulk is already scheduled to start at the same time with the same player
     * <li> you have 10 scheduled bulks
     * <li> you have 1000 scheduled games
     * </ul>
     * Partial bulks are never created. Either it all fails, or it all
     * succeeds. When it fails, it does so with an error message explaining the
     * issue. Failed bulks are not counted in the rate limiting, they are free.
     * Fix the issues, manually or programmatically, then retry to schedule the
     * bulk.<br>
     * A successful bulk creation returns the created bulk.<br>
     * Its ID can be used for further operations.
     */
    Result<BulkPairing> createBulk(Function<BulkBBuilder, BulkBuilder> params);
    Result<Ack>         startBulk(String bulkId);
    Result<Ack>         cancelBulk(String bulkId);



    interface BulkBBuilder {

        /**
         * @param clockInitial Clock initial time in seconds [ 0 .. 10800 ]
         * @param clockIncrement Clock increment in seconds [ 0 .. 60 ]
         */
        public BulkBuilder clock(int clockInitial, int clockIncrement);

    }

    interface BulkBuilder {
        record Pairing(Supplier<char[]> tokenWhite, Supplier<char[]> tokenBlack) {}

        /**
         * @param tokenWhite
         * @param tokenBlack
         */
        BulkBuilder addPairing(String tokenWhite, String tokenBlack);

        /**
         * @param tokenWhite
         * @param tokenBlack
         */
        BulkBuilder addPairing(Supplier<char[]> tokenWhite, Supplier<char[]> tokenBlack);

        BulkBuilder rated(boolean rated);
        BulkBuilder addPairing(Pairing pairing);

        /**
         * @param pairAt Date at which the games will be created.<br>
         *               Up to 24h in the future.<br>
         *               Omit, or set to current date and time, to start the games immediately.
         */
        BulkBuilder pairAt(ZonedDateTime pairAt);

        /**
         * @param startClocksAt Date at which the clocks will be automatically started.<br>
         *                      Up to 24h in the future.<br>
         *                      Note that the clocks can start earlier than specified, if players start making moves in the game.<br>
         *                      If omitted, the clocks will not start automatically.
         */
        BulkBuilder startClocksAt(ZonedDateTime startClocksAt);

        /**
         * @param variant The variant to use in games
         */
        BulkBuilder variant(VariantName variant);

        /**
         * @param variant The variant to use in games
         */
        BulkBuilder variant(Function<VariantName.Provider, VariantName> variant);

        /**
         * @param message Message that will be sent to each player, when the game is created.<br>
         *                It is sent from your user account.<br>
         *                Default: "Your game with {opponent} is ready: {game}."
         */
        BulkBuilder message(String message);
    }
}
