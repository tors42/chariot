package chariot;

import java.util.Collection;
import java.util.prefs.Preferences;

import chariot.Client.Scope;
import chariot.api.*;
import chariot.api.Analysis;
import chariot.internal.*;
import chariot.internal.impl.*;
import chariot.model.*;

/**
 * {@code ClientAuth} provides authenticated access to the <a href="https://lichess.org/api">Lichess API</a>.
 */
public class ClientAuth extends ClientBase  {

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
     * {@inheritDoc} <br>
     * Send messages to users.
     */
    public UsersAuth users() {
        return usersHandler;
    }

    /**
     * Read and write account informations and preferences.
     */
    public AccountAuth account() { return accountHandler; }

    /**
     * For administrators only, to obtain challenge tokens.
     */
    public AdminAuth admin() { return adminHandler; }

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
    public BoardAuth board() { return boardHandler; }

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
    public BotAuth bot() { return botHandler; }

    /**
     * {@inheritDoc}
     */
    public BroadcastsAuth broadcasts() { return broadcastsHandler; }

    /**
     * Send and receive challenges and manage bulk challenges.
     */
    public ChallengesAuth challenges() { return challengesHandler; }

    /**
     * {@inheritDoc}
     */
    public ExternalEngineAuth externalEngine() { return externalEngineHandler; }

    /**
     * {@inheritDoc}
     */
    public GamesAuth games() { return gamesHandler; }

    /**
     * {@inheritDoc}
     */
    public PuzzlesAuth puzzles() { return puzzlesHandler; }
    /**
     * {@inheritDoc}
     */
    public TeamsAuth teams() { return teamsHandler; }

    /**
     * {@inheritDoc} <br>
     * Official tournaments are maintained by Lichess, but you can create your own tournaments as well.
     */
    public TournamentsAuth tournaments() { return tournamentsHandler; }


    /** {@inheritDoc} */ @Override public Analysis analysis() { return super.analysis(); }
    /** {@inheritDoc} */ @Override public OpeningExplorer openingExplorer() { return super.openingExplorer(); }
    /** {@inheritDoc} */ @Override public Simuls simuls() { return super.simuls(); }
    /** {@inheritDoc} */ @Override public Studies studies() { return super.studies(); }
    /** {@inheritDoc} */ @Override public Tablebase tablebase() { return super.tablebase(); }
    /** {@inheritDoc} */ @Override public Custom custom() { return super.custom(); }



    /**
     * Read which scopes are available with current token
     */
    public Collection<Scope> scopes() { return tokenHandler.scopes(); }
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
