package chariot.api;

import java.util.function.*;

import chariot.model.*;
import chariot.model.Enums.*;
import chariot.api.Builders.*;

public interface ChallengesAuthCommon {

    Many<StreamEvent> streamEvents();

    /**
     * The challenge expires after 20s if not accepted.<br>
     */
    One<Challenge> challenge(String userId, Consumer<ChallengeBuilder> params);

    /**
     * The challenge is kept alive until the stream is closed.<br>
     */
    Many<Challenge> challengeKeepAlive(String userId, Consumer<ChallengeBuilder> params);

    One<ChallengeAI> challengeAI(Consumer<ChallengeAIBuilder> params);
    One<Ack>         cancelChallenge(String challengeId);
    One<Ack>         cancelChallenge(String challengeId, Supplier<char[]> opponentToken);
    One<Ack>         acceptChallenge(String challengeId);
    One<Ack>         declineChallenge(String challengeId);
    One<Ack>         declineChallenge(String challengeId, DeclineReason reason);

    default One<Ack> declineChallenge(String challengeId, Function<DeclineReason.Provider, DeclineReason> reason) {
        return declineChallenge(challengeId, reason.apply(DeclineReason.provider()));
    }

    interface ChallengeBuilder extends Clock<ChallengeParams>, ClockCorrespondence<ChallengeParams> {}

    interface ChallengeParams {

        ChallengeParams rated(boolean rated);

        /**
         * @param color Which color you get to play,
         * if the challenge is accepted.
         */
        ChallengeParams color(ColorPref color);

        /**
         * @param variant The variant to use in games
         */
        ChallengeParams variant(VariantName variant);

        /**
         * @param fen Custom initial position (in FEN).
         * <p>Default: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"<br/>
         * Variant must be standard, and the game cannot be rated.<br/>
         * Castling moves will use UCI_Chess960 notation, for example <code>e1h1</code> instead of <code>e1g1</code>.<br/>
         */
        ChallengeParams fen(String fen);

        /**
         * Immediately accept the challenge and create the game.
         * @param acceptByToken Pass in an OAuth token (with the challenge:write scope) for the receiving user.
         * <p>On success, the response will contain a game field instead of a challenge field.<br/>
         * Alternatively, consider the bulk pairing API.
         */
        ChallengeParams acceptByToken(String acceptByToken);

        /**
         * Immediately accept the challenge and create the game.
         * @param acceptByToken Pass in an OAuth token (with the challenge:write scope) for the receiving user.
         * On success, the response will contain a game field instead of a challenge field.
         * Alternatively, consider the bulk pairing API.
         *
         * @param message Message that is sent to each player, when the game is created.
         * Default: "Your game with {opponent} is ready: {game}."
         * It is sent from your user account.
         * {opponent}, {player} and {game} are placeholders that will be replaced with
         * the opponent name, player name, and the game URLs.
         * You can omit this field to send the default message - see <code>acceptByToken(String acceptByToken)</code>,
         * but if you set your own message, it must at least contain the {game} placeholder.
         */
        ChallengeParams acceptByToken(String acceptByToken, String message);


        default ChallengeParams color(Function<ColorPref.Provider, ColorPref> color) { return color(color.apply(ColorPref.provider())); }
        default ChallengeParams variant(Function<VariantName.Provider, VariantName> variant) {return variant(variant.apply(VariantName.provider())); }
    }


    interface ChallengeAIBuilder extends Clock<ChallengeAIParams>, ClockCorrespondence<ChallengeAIParams> {}

    interface ChallengeAIParams {

        /**
         * @param level AI strength
         */
        ChallengeAIParams level(Level level);
        default ChallengeAIParams level(Function<Level.Provider, Level> level) { return level(level.apply(Level.provider())); }

        /**
         * @param color Which color you get to play,
         * if the challenge is accepted.
         */
        ChallengeAIParams color(ColorPref color);
        default ChallengeAIParams color(Function<ColorPref.Provider, ColorPref> color) { return color(color.apply(ColorPref.provider())); }


        ChallengeAIParams variant(VariantName variant);
        default ChallengeAIParams variant(Function<VariantName.Provider, VariantName> variant) { return variant(variant.apply(VariantName.provider())); };

        /**
         * @param fen Custom initial position (in FEN).
         * <p>Default: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"<br/>
         * Variant must be standard, and the game cannot be rated.<br/>
         * Castling moves will use UCI_Chess960 notation, for example <code>e1h1</code> instead of <code>e1g1</code>.<br/>
         */
        ChallengeAIParams fen(String fen);
    }

}

