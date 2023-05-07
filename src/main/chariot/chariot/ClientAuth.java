package chariot;

import java.util.Collection;

import chariot.api.*;
import chariot.model.*;

/**
 * {@code ClientAuth} provides authenticated access to the <a href="https://lichess.org/api">Lichess API</a>.
 */
public sealed interface ClientAuth extends Client permits chariot.internal.Default {

    /**
     * {@inheritDoc} <br>
     * Send messages to users.
     */
    UsersAuth users();

    /**
     * Read which scopes are available with current token
     */
    Collection<Scope> scopes();
    /**
     * Revokes the access token sent as Bearer for this request.
     */
    One<Void> revokeToken();

    /**
     * Read and write account informations and preferences.
     */
    AccountAuth account();

    /**
     * For administrators only, to obtain challenge tokens.
     */
    AdminAuth admin();

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
    BoardAuth board();

    /**
     * Play on Lichess as a bot.
     * <p>Allows engine play.<br/>
     * Only works with Bot accounts.
     * <p><b>Features</b>
     * <ul>
     * <li>Stream incoming chess moves
     * <li>Play chess moves
     * <li>Read and write in the player and spectator chats
     * <li>Receive, create and accept (or decline) challenges
     * <li>Abort and resign games
     * <li>Engine assistance is allowed
     * </ul>
     * <p><b>Restrictions</b>
     * <ul>
     * <li>Bots can only play challenge games: pools and tournaments are off-limits
     * <li>Bots cannot play UltraBullet (¼+0) because it requires making too many requests. But 0+1 and ½+0 are allowed.
     * </ul>
     */
    BotAuth bot();

    /**
     * {@inheritDoc}
     */
    BroadcastsAuth broadcasts();

    /**
     * Send and receive challenges and manage bulk challenges.
     */
    ChallengesAuth challenges();

    /**
     * {@inheritDoc}
     */
    ExternalEngineAuth externalEngine();

    /**
     * {@inheritDoc}
     */
    GamesAuth games();

    /**
     * {@inheritDoc}
     */
    PuzzlesAuth puzzles();
    /**
     * {@inheritDoc}
     */
    TeamsAuth teams();

    /**
     * {@inheritDoc} <br>
     * Official tournaments are maintained by Lichess, but you can create your own tournaments as well.
     */
    TournamentsAuth tournaments();


}
