package chariot.api;

import java.util.function.Consumer;
import java.util.function.Function;

import chariot.model.ChallengeResult.ChallengeOpenEnded;
import chariot.model.Enums.VariantName;
import chariot.api.Builders.Clock;
import chariot.api.Builders.ClockCorrespondence;
import chariot.model.Result;

public interface Challenges {

    Result<ChallengeOpenEnded> challengeOpenEnded(Consumer<OpenEndedBuilder> params);

    interface OpenEndedBuilder extends Clock<OpenEndedParams>, ClockCorrespondence<OpenEndedParams> {}

    interface OpenEndedParams {
        OpenEndedParams rated(boolean rated);
        OpenEndedParams variant(VariantName variant);
        /**
         * @param fen Custom initial position (in FEN).
         * Default: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
         * Variant must be standard, and the game cannot be rated.
         * Castling moves will use UCI_Chess960 notation, for example e1h1 instead of e1g1.
         */
        OpenEndedParams fen(String fen);

        /**
         * @param name Optional name for the challenge, that players will see on the challenge page.
         */
        OpenEndedParams name(String name);

        default OpenEndedParams variant(Function<VariantName.Provider, VariantName> variant) {return variant(variant.apply(VariantName.provider())); }
    }
}
