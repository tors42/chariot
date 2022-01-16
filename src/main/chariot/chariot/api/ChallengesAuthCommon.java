package chariot.api;

import java.util.function.Function;
import java.util.function.Supplier;

import chariot.model.Enums.*;
import chariot.model.Ack;
import chariot.model.ChallengeResult.ChallengeAI;
import chariot.model.ChallengeResult.Challenge;
import chariot.model.Result;
import chariot.model.StreamEvent;

public interface ChallengesAuthCommon {

    Result<StreamEvent> streamEvents();
    Result<Challenge>   challenge(String userId, Function<ChallengeBBuilder, ChallengeBuilder> params);
    Result<ChallengeAI> challengeAI(Function<ChallengeAIBBuilder, ChallengeAIBuilder> params);
    Result<Ack>         cancelChallenge(String challengeId);
    Result<Ack>         cancelChallenge(String challengeId, Supplier<char[]> opponentToken);
    Result<Ack>         acceptChallenge(String challengeId);
    Result<Ack>         declineChallenge(String challengeId);
    Result<Ack>         declineChallenge(String challengeId, DeclineReason reason);

    default Result<Ack> declineChallenge(String challengeId, Function<DeclineReason.Provider, DeclineReason> reason) {
        return declineChallenge(challengeId, reason.apply(DeclineReason.provider()));
    }

    interface ChallengeBBuilder {
        /**
         * Correspondence challenge
         * @param d
         */
        ChallengeBuilder clock(DaysPerTurn d);

        /**
         * Real-time challenge
         * @param clockInitial Clock initial time in seconds [ 0 .. 10800 ]
         * @param clockIncrement Clock increment in seconds [ 0 .. 60 ]
         */
        ChallengeBuilder clock(int clockInitial, int clockIncrement);


        default ChallengeBuilder clock(Function<DaysPerTurn.Provider, DaysPerTurn> d) { return clock(d.apply(DaysPerTurn.provider())); }
    }

    interface ChallengeBuilder {

        ChallengeBuilder rated(boolean rated);

        /**
         * @param color Which color you get to play,
         * if the challenge is accepted.
         */
        ChallengeBuilder color(ColorPref color);

        /**
         * @param variant The variant to use in games
         */
        ChallengeBuilder variant(VariantName variant);

        /**
         * @param fen Custom initial position (in FEN).
         * <p>Default: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"<br/>
         * Variant must be standard, and the game cannot be rated.<br/>
         * Castling moves will use UCI_Chess960 notation, for example <code>e1h1</code> instead of <code>e1g1</code>.<br/>
         */
        ChallengeBuilder fen(String fen);

        /**
         * @param keepAliveStream If set, the response is streamed. The challenge is kept alive
         * until the stream is closed.<br>
         * If not set, the response is not streamed, and the challenge expires after 20s if not accepted.<br>
         *
         * Implementation note for stream: When the challenge is accepted, declined or canceled,
         * a message of the form {"done":"accepted"} is sent, then the connection is closed by the server -
         * but this info will be silently dropped - you will still receive this info at {@link #streamEvents()}
         */
        ChallengeBuilder keepAliveStream(boolean keepAliveStream);

        /**
         * Immediately accept the challenge and create the game.
         * @param acceptByToken Pass in an OAuth token (with the challenge:write scope) for the receiving user.
         * <p>On success, the response will contain a game field instead of a challenge field.<br/>
         * Alternatively, consider the bulk pairing API.
         */
        ChallengeBuilder acceptByToken(String acceptByToken);

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
        ChallengeBuilder acceptByToken(String acceptByToken, String message);


        default ChallengeBuilder color(Function<ColorPref.Provider, ColorPref> color) { return color(color.apply(ColorPref.provider())); }
        default ChallengeBuilder variant(Function<VariantName.Provider, VariantName> variant) {return variant(variant.apply(VariantName.provider())); }
    }


    interface ChallengeAIBBuilder {
        /**
         * Correspondence challenge
         * @param d
         */
        ChallengeAIBuilder clock(DaysPerTurn d);

        /**
         * Real-time challenge
         * @param clockInitial Clock initial time in seconds [ 0 .. 10800 ]
         * @param clockIncrement Clock increment in seconds [ 0 .. 60 ]
         */
        ChallengeAIBuilder clock(int clockInitial, int clockIncrement);


        default ChallengeAIBuilder clock(Function<DaysPerTurn.Provider, DaysPerTurn> d) { return clock(d.apply(DaysPerTurn.provider())); }

    }

    interface ChallengeAIBuilder {

        /**
         * @param level AI strength
         */
        ChallengeAIBuilder level(Level level);

        /**
         * @param color Which color you get to play,
         * if the challenge is accepted.
         */
        ChallengeAIBuilder color(ColorPref color);


        ChallengeAIBuilder variant(VariantName variant);

        /**
         * @param fen Custom initial position (in FEN).
         * <p>Default: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"<br/>
         * Variant must be standard, and the game cannot be rated.<br/>
         * Castling moves will use UCI_Chess960 notation, for example <code>e1h1</code> instead of <code>e1g1</code>.<br/>
         */
        ChallengeAIBuilder fen(String fen);


        default ChallengeAIBuilder level(Function<Level.Provider, Level> level) { return level(level.apply(Level.provider())); }
        default ChallengeAIBuilder color(Function<ColorPref.Provider, ColorPref> color) { return color(color.apply(ColorPref.provider())); }
        default ChallengeAIBuilder variant(Function<VariantName.Provider, VariantName> variant) { return variant(variant.apply(VariantName.provider())); };
    }

}

