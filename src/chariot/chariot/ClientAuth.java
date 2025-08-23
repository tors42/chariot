package chariot;

import module chariot;

/**
 * {@code ClientAuth} provides authenticated access to the <a href="https://lichess.org/api">Lichess API</a>.
 */
public interface ClientAuth extends Client {

    /// Read and write account informations and preferences.
    AccountApiAuth account();

    /// For administrators only, to obtain challenge tokens.
    AdminApiAuth admin();

    /// Play on Lichess with physical boards and third-party clients.
    ///
    /// Engine play or assistance is forbidden.
    ///
    /// Works with normal Lichess accounts.
    ///
    /// **Features**
    /// + Stream incoming chess moves
    /// + Play chess moves
    /// + Read and write in the player and spectator chats
    /// + Receive, create and accept (or decline) challenges
    /// + Abort and resign games
    /// + Compatible with normal Lichess accounts
    ///
    /// **Restrictions**
    /// - Engine assistance, or any kind of outside help, is strictly forbidden
    /// - Time controls: Rapid, Classical and Correspondence only (Blitz possible for direct challanges and vs AI)
    BoardApiAuth board();

    /// Play on Lichess as a bot.
    ///
    /// Allows engine play.
    ///
    /// Only works with Bot accounts.
    ///
    /// **Features**
    /// + Stream incoming chess moves
    /// + Play chess moves
    /// + Read and write in the player and spectator chats
    /// + Receive, create and accept (or decline) challenges
    /// + Abort and resign games
    /// + Engine assistance is allowed
    ///
    /// **Restrictions**
    /// - Bots can only play challenge games: pools and tournaments are off-limits
    /// - Bots cannot play UltraBullet (¼+0) because it requires making too many requests. But 0+1 and ½+0 are allowed.
    BotApiAuth bot();


    /// {@inheritDoc}
    @Override UsersApiAuth users();

    /// {@inheritDoc}
    @Override BroadcastsApiAuth broadcasts();

    /// Send and receive challenges and manage bulk challenges.
    @Override ChallengesApiAuth challenges();

    /// {@inheritDoc}
    @Override ExternalEngineApiAuth externalEngine();

    /// {@inheritDoc}
    @Override GamesApiAuth games();

    /// {@inheritDoc}
    @Override PuzzlesApiAuth puzzles();

    /// {@inheritDoc}
    @Override StudiesApiAuth studies();

    /// {@inheritDoc}
    @Override TeamsApiAuth teams();

    /// {@inheritDoc}
    @Override TournamentsApiAuth tournaments();


    /// Fetch which scopes are available with current token.<br>
    /// Note, a token can be revoked externally by user at any time.
    Many<Scope> scopes();

    /// Revokes the access token sent as Bearer for this request.
    Ack revokeToken();

    ///  Clears client token information from preferences.<br>
    ///  See {@link Client#load(Preferences)}
    ///  @param prefs The preferences node to clear
    void clearAuth(Preferences prefs);

}
