package chariot.api;

import java.util.function.*;
import java.util.function.Function;

import chariot.api.Builders.*;
import chariot.model.*;
import chariot.model.Enums.*;

/**
 * Play on Lichess with physical boards and third-party clients.
 * <p>Works with normal Lichess accounts.<br/>
 * Engine play or assistance is forbidden.
 * <p><b>Features</b>
 * <ul>
 * <li>Stream incoming chess moves
 * <li>Play chess moves
 * <li>Read and write in the player and spectator chats
 * <li>Receive, create and accept (or decline) challenges
 * <li>Abort and resign games
 * <li>Compatible with normal Lichess accounts
 * </ul>
 * <p><b>Restrictions</b>
 * <ul>
 * <li>Engine assistance, or any kind of outside help, is strictly forbidden
 * <li>Time controls: Rapid, Classical and Correspondence only (Blitz possible for direct challanges and vs AI)
 * </ul>
 */
public interface BoardApiAuth extends ChallengesApiAuthCommon {

    /**
     * Create a public seek, to start a game with a random player.
     *
     * <p><b>Real-time seek:</b><br/>
     * Specify the time and increment clock values.<br/>
     * The response is streamed but doesn't contain any information.<br/>
     * Keep the connection open to keep the seek active.<br/>
     * If the client closes the connection, the seek is canceled.<br/>
     * This way, if the client terminates, the user won't be paired in a game they wouldn't play.<br/>
     * When the seek is accepted, or expires, the server closes the connection.<br/>
     * Make sure to also have an Event stream open, to be notified when a game starts.<br/>
     * We recommend opening the Event stream first, then the seek stream.<br/>
     * This way, you won't miss the game event if the seek is accepted immediately.<br/>
     */
    Many<String> seekRealTime(Consumer<SeekRealTimeBuilder> params);
    default Many<String> seekRealTime(int initial, int increment) {
        return seekRealTime(params -> params.clock(initial, increment));
    }

    /**
     * Create a public correspondence seek, to start a game with a random player.
     *
     * <p><b>Correspondence seek:</b><br/>
     * Specify the days per turn value. The response is not streamed, it immediately completes with the seek ID.<br/>
     * The seek remains active on the server until it is joined by someone.<br/>
     */
    One<SeekAck> seekCorrespondence(Consumer<SeekCorrespondenceBuilder> params);
    default One<SeekAck> seekCorrespondence(int daysPerTurn) {
        return seekCorrespondence(params -> params.daysPerTurn(daysPerTurn));
    }

    /**
     * Stream the state of a game being played with the Board API
     * <p>Use this endpoint to get updates about the game in real-time, with a single request.
     * <p>Event types
     * <ul>
     * <li> gameFull Full game data.
     * <li> gameState Current state of the game. Sent when a move is played, a draw is offered, or when the game ends.
     * <li> chatLine Chat message sent by a user in the {@code room} player or spectator.
     * </ul>
     * <p> The first event is always of type gameFull
     *  @param gameId Example: 5IrD6Gzz
     */
    Many<GameStateEvent> connectToGame(String gameId);

    /**
     *  Make a move in a game being played with the Board API.<br/>
     *
     *  @param gameId Example: 5IrD6Gzz
     *  @param move The move to play, in UCI format. Example: e2e4
     *  @param drawOffer Whether to offer (or agree to) a draw
     */
    Ack move(String gameId, String move, boolean drawOffer);
    /**
     *  Make a move in a game being played with the Board API.<br/>
     *  The move can also contain a draw offer/agreement. {@link #move(String, String, boolean) move(..., boolean)}
     *
     *  @param gameId Example: 5IrD6Gzz
     *  @param move The move to play, in UCI format. Example: e2e4
     */
    Ack move(String gameId, String move);

    /**
     * Post a message to the player chat, in a game being played with the Board API.
     * @param gameId  Example: 5IrD6Gzz
     * @param text
     */
    Ack chat(String gameId, String text);

    /**
     * Post a message to the spectator chat, in a game being played with the Board API.
     * @param gameId  Example: 5IrD6Gzz
     * @param text
     */
    Ack chatSpectators(String gameId, String text);


    /**
     * Abort a game being played with the Board API.
     * @param gameId  Example: 5IrD6Gzz
     */
    Ack abort(String gameId);

    /**
     * Resign a game being played with the Board API.
     * @param gameId  Example: 5IrD6Gzz
     */
    Ack resign(String gameId);

    /**
     * Create/accept/decline draw offers.<br>
     *
     * Offer a draw, or accept the opponent's draw offer<br>
     * Decline a draw offer from the opponent<br>
     *
     * @param gameId Example: 5IrD6Gzz
     * @param offerOrAccept true to offer or accept a draw offer, false to decline a draw offer
     */
    Ack handleDrawOffer(String gameId, boolean offerOrAccept);

    /**
     * Create/accept/decline takeback offers.<br>
     *
     * Offer a takeback, or accept the opponent's takeback offer<br>
     * Decline a takeback offer from the opponent<br>
     *
     * @param gameId Example: 5IrD6Gzz
     * @param offerOrAccept true to offer or accept a takeback offer, false to decline a takeback offer
     */
    Ack handleTakebackOffer(String gameId, boolean offerOrAccept);


    /**
     * Claim victory when the opponent has left the game for a while.
     * @param gameId  Example: 5IrD6Gzz
     */
    Ack claimVictory(String gameId);

    /// Claim draw when the opponent has left the game for a while.
    /// @param gameId  Example: 5IrD6Gzz
    Ack claimDraw(String gameId);

    /**
     * Get the messages posted in the game chat.
     * @param gameId  Example: 5IrD6Gzz
     */
    Many<ChatMessage> fetchChat(String gameId);

    /**
     * Go berserk on an arena tournament game. Halves the clock time, grants an extra point upon winning.
     * Only available in arena tournaments that allow berserk, and before each player has made a move.
     * @param gameId  Example: 5IrD6Gzz
     */
    Ack berserk(String gameId);


    interface SeekRealTimeBuilder extends ClockSeek<SeekParams> {}
    interface SeekCorrespondenceBuilder extends ClockCorrespondence<SeekCorrParams> {}

    interface SeekParams {
        /**
         * @param color Which color you get to play,
         */
        SeekParams color(ColorPref color);
        default SeekParams color(Function<ColorPref.Provider, ColorPref> color) { return color(color.apply(ColorPref.provider())); }

        SeekParams variant(VariantName variant);
        default SeekParams variant(Function<VariantName.Provider, VariantName> variant) { return variant(variant.apply(VariantName.provider())); }
        SeekParams rated(boolean rated);
        default SeekParams rated() { return rated(true); }
        /**
         * The rating range of potential opponents. Better left empty. Example: 1500-1800
         */
        SeekParams ratingRange(String ratingRange);
    }

    interface SeekCorrParams {
        SeekCorrParams variant(VariantName variant);
        default SeekCorrParams variant(Function<VariantName.Provider, VariantName> variant) { return variant(variant.apply(VariantName.provider())); }
        SeekCorrParams rated(boolean rated);
        default SeekCorrParams rated() { return rated(true); }
        /**
         * The rating range of potential opponents. Better left empty. Example: 1500-1800
         */
        SeekCorrParams ratingRange(String ratingRange);
    }

}
