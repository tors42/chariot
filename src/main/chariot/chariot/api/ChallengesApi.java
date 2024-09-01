package chariot.api;

import java.time.Duration;
import java.util.function.*;

import chariot.model.ChallengeOpenEnded;
import chariot.model.One;
import chariot.model.Enums.VariantName;
import chariot.api.Builders.*;

public interface ChallengesApi {

    One<ChallengeOpenEnded> challengeOpenEnded(Consumer<OpenEndedBuilder> params);

    interface OpenEndedBuilder extends ClockBuilder<OpenEndedParams>, ClockCorrespondence<OpenEndedParams> {}

    interface OpenEndedParams {
        OpenEndedParams rated(boolean rated);
        default OpenEndedParams rated() { return rated(true); }
        OpenEndedParams variant(VariantName variant);
        default OpenEndedParams variant(Function<VariantName.Provider, VariantName> variant) { return variant(variant.apply(VariantName.provider())); }

        /**
         * @param fen Custom initial position (in FEN).
         * Default: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
         * Variant must be standard, fromPosition, or chess960 (if a valid 960 starting positiion), and the game cannot be rated.
         * Castling moves will use UCI_Chess960 notation, for example e1h1 instead of e1g1.
         */
        OpenEndedParams fen(String fen);

        /**
         * @param name Optional name for the challenge, that players will see on the challenge page.
         */
        OpenEndedParams name(String name);

        /**
         * Optionally specify two usernames. Only these users may join the challenge.
         * @param white the username which plays white pieces
         * @param black the username which plays black pieces
         */
        OpenEndedParams users(String white, String black);

        OpenEndedParams noAbort();
        OpenEndedParams noRematch();
        OpenEndedParams noGiveTime();
        OpenEndedParams noClaimWin();
        OpenEndedParams noEarlyDraw();

        /**
         * Defaults to 24h after creation. Can't be more than 2 weeks after creation.
         */
        OpenEndedParams expiresIn(Duration expiresIn);
    }
}
