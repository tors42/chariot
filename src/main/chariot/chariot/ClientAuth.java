package chariot;

import java.util.prefs.Preferences;

import chariot.api.*;
import chariot.api.AnalysisApi;
import chariot.internal.*;
import chariot.internal.impl.*;
import chariot.model.*;

/**
 * {@code ClientAuth} provides authenticated access to the <a href="https://lichess.org/api">Lichess API</a>.
 */
public class ClientAuth extends Client {

    final AccountHandler accountHandler;
    final AdminHandler adminHandler;
    final BoardHandler boardHandler;
    final UsersAuthHandler usersHandler;

    ClientAuth(Config config) {
        super(config);
        accountHandler = new AccountHandler(requestHandler());
        adminHandler = new AdminHandler(requestHandler());
        boardHandler = new BoardHandler(requestHandler());
        usersHandler = new UsersAuthHandler(requestHandler());
    }

    /**
     * Access registered users on Lichess.
     * Send messages to users.
     */
    public UsersApiAuth usersAuth() {
        return usersHandler;
    }

    /**
     * Read and write account informations and preferences.
     */
    public AccountApiAuth account() { return accountHandler; }

    /**
     * For administrators only, to obtain challenge tokens.
     */
    public AdminApiAuth admin() { return adminHandler; }

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
    public BoardApiAuth board() { return boardHandler; }

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
    public BotApiAuth bot() { return botHandler; }

    /**
     * {@inheritDoc}
     */
    public BroadcastsApiAuth broadcasts() { return broadcastsHandler; }

    /**
     * Send and receive challenges and manage bulk challenges.
     */
    public ChallengesApiAuth challenges() { return challengesHandler; }

    /**
     * {@inheritDoc}
     */
    public ExternalEngineApiAuth externalEngine() { return externalEngineHandler; }

    /**
     * {@inheritDoc}
     */
    public GamesApiAuth games() { return gamesHandler; }

    /**
     * {@inheritDoc}
     */
    public PuzzlesApiAuth puzzles() { return puzzlesHandler; }

    /**
     * {@inheritDoc}
     **/
    public StudiesApiAuth studies() { return studiesHandler; }

    /**
     * {@inheritDoc}
     */
    public TeamsApiAuth teams() { return teamsHandler; }

    /**
     * {@inheritDoc} <br>
     * Official tournaments are maintained by Lichess, but you can create your own tournaments as well.
     */
    public TournamentsApiAuth tournaments() { return tournamentsHandler; }


    /** {@inheritDoc} */ @Override public AnalysisApi analysis() { return super.analysis(); }
    /** {@inheritDoc} */ @Override public OpeningExplorerApi openingExplorer() { return super.openingExplorer(); }
    /** {@inheritDoc} */ @Override public SimulsApi simuls() { return super.simuls(); }
    /** {@inheritDoc} */ @Override public TablebaseApi tablebase() { return super.tablebase(); }
    /** {@inheritDoc} */ @Override public CustomApi custom() { return super.custom(); }


    /**
     * Fetch which scopes are available with current token.<br>
     * Note, a token can be revoked externally by user at any time.
     */
    public Many<Scope> scopes() { return tokenHandler.scopes(); }

    /**
     * Revokes the access token sent as Bearer for this request.
     */
    public One<Void> revokeToken() { return tokenHandler.revokeToken(); }

    /**
     * Clears client token information from preferences.<br>
     * See {@link Client#load(Preferences)}
     * @param prefs The preferences node to clear
     */
    public void clearAuth(Preferences prefs) { Config.clearAuth(prefs); }

}
