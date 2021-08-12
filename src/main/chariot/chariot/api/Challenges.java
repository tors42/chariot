package chariot.api;

import java.util.function.Function;

import chariot.model.ChallengeResult.ChallengeOpenEnded;
import chariot.model.Enums.VariantName;
import chariot.model.Result;

public interface Challenges {

    Result<ChallengeOpenEnded> challengeOpenEnded(Function<OpenEndedBBuilder, OpenEndedBuilder> params);

    interface OpenEndedBBuilder {
        /**
         * Realtime challenge
         * @param clockInitial Clock initial time in seconds [ 0 .. 10800 ]
         * @param clockIncrement Clock increment in seconds [ 0 .. 60 ]
         */
        OpenEndedBuilder clock(int clockInitial, int clockIncrement);
    }

    interface OpenEndedBuilder {
        OpenEndedBuilder rated(boolean rated);
        OpenEndedBuilder variant(VariantName variant);
        OpenEndedBuilder variant(Function<VariantName.Provider, VariantName> variant);
        /**
         * @param fen Custom initial position (in FEN).
         * Default: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
         * Variant must be standard, and the game cannot be rated.
         * Castling moves will use UCI_Chess960 notation, for example e1h1 instead of e1g1.
         */
        OpenEndedBuilder fen(String fen);

        /**
         * @param name Optional name for the challenge, that players will see on the challenge page.
         */
        OpenEndedBuilder name(String name);

    }
}
