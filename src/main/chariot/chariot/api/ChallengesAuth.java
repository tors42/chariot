package chariot.api;

import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import chariot.model.Enums.*;
import chariot.api.Builders.Clock;
import chariot.internal.Crypt;
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
    Result<BulkPairing> createBulk(Consumer<BulkBuilder> params);
    Result<Ack>         startBulk(String bulkId);
    Result<Ack>         cancelBulk(String bulkId);



    interface BulkBuilder extends Clock<BulkParams> {}

    interface BulkParams {

        BulkParams rated(boolean rated);

        /**
         * @param tokenWhite
         * @param tokenBlack
         */
        default BulkParams addPairing(String tokenWhite, String tokenBlack) {
            // User is supplying plain text tokens.
            // Let's make an effort, albeit small, and obfuscate the tokens
            // so we don't keep them in memory in plain text.
            var encWhite = Crypt.encrypt(tokenWhite.toCharArray());
            var encBlack = Crypt.encrypt(tokenBlack.toCharArray());
            return addPairing(new Pairing(
                        () -> Crypt.decrypt(encWhite.data(), encWhite.key()),
                        () -> Crypt.decrypt(encBlack.data(), encBlack.key())
                        )
                    );
        }

        /**
         * @param tokenWhite
         * @param tokenBlack
         */
        default BulkParams addPairing(Supplier<char[]> tokenWhite, Supplier<char[]> tokenBlack) {
            return addPairing(new Pairing(tokenWhite, tokenBlack));
        }


        BulkParams addPairing(Pairing pairing);

        /**
         * @param pairAt Date at which the games will be created.<br>
         *               Up to 24h in the future.<br>
         *               Omit, or set to current date and time, to start the games immediately.
         */
        BulkParams pairAt(long pairAt);

        /**
         * @param pairAt Date at which the games will be created.<br>
         *               Up to 24h in the future.<br>
         *               Omit, or set to current date and time, to start the games immediately.
         */
         default BulkParams pairAt(ZonedDateTime pairAt) { return pairAt(pairAt.toInstant().toEpochMilli()); }

        /**
         * @param startClocksAt Date at which the clocks will be automatically started.<br>
         *                      Up to 24h in the future.<br>
         *                      Note that the clocks can start earlier than specified, if players start making moves in the game.<br>
         *                      If omitted, the clocks will not start automatically.
         */
        BulkParams startClocksAt(long startClocksAt);

        /**
         * @param startClocksAt Date at which the clocks will be automatically started.<br>
         *                      Up to 24h in the future.<br>
         *                      Note that the clocks can start earlier than specified, if players start making moves in the game.<br>
         *                      If omitted, the clocks will not start automatically.
         */
        default BulkParams startClocksAt(ZonedDateTime startClocksAt) { return startClocksAt(startClocksAt.toInstant().toEpochMilli()); }

        /**
         * @param variant The variant to use in games
         */
        BulkParams variant(VariantName variant);

        /**
         * @param variant The variant to use in games
         */
        default BulkParams variant(Function<VariantName.Provider, VariantName> variant) { return variant(variant.apply(VariantName.provider())); }

        /**
         * @param message Message that will be sent to each player, when the game is created.<br>
         *                It is sent from your user account.<br>
         *                Default: "Your game with {opponent} is ready: {game}."
         */
        BulkParams message(String message);

        record Pairing(Supplier<char[]> tokenWhite, Supplier<char[]> tokenBlack) {}
    }
}
