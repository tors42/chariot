package chariot.internal.impl;

import module chariot;

import chariot.internal.Config;

public class ClientAuthImpl extends ClientImpl implements ClientAuth {

    final AccountHandler accountHandler;
    final AdminHandler adminHandler;
    final BoardHandler boardHandler;
    final UsersAuthHandler usersHandler;

    public ClientAuthImpl(Config config) {
        super(config);
        accountHandler = new AccountHandler(requestHandler());
        adminHandler = new AdminHandler(requestHandler());
        boardHandler = new BoardHandler(requestHandler());
        usersHandler = new UsersAuthHandler(requestHandler());
    }

    // ClientAuth
    @Override public AccountApiAuth account() { return accountHandler; }
    @Override public AdminApiAuth admin() { return adminHandler; }
    @Override public BoardApiAuth board() { return boardHandler; }
    @Override public UsersApiAuth users() { return usersHandler; }
    @Override public OAuthAuthApi oauth() { return oAuthHandler; }

    @Override public Many<Scope> scopes() { return oAuthHandler.scopes(); }
    @Override public Ack revokeToken() { return oAuthHandler.revokeToken(); }
    @Override public void clearAuth(Preferences prefs) { Config.clearAuth(prefs); }

    // Client
    @Override public BotApiAuth bot() { return botHandler; }
    @Override public BroadcastsApiAuth broadcasts() { return broadcastsHandler; }
    @Override public ChallengesApiAuth challenges() { return challengesHandler; }
    @Override public ExternalEngineApiAuth externalEngine() { return externalEngineHandler; }
    @Override public GamesApiAuth games() { return gamesHandler; }
    @Override public PuzzlesApiAuth puzzles() { return puzzlesHandler; }
    @Override public StudiesApiAuth studies() { return studiesHandler; }
    @Override public TeamsApiAuth teams() { return teamsHandler; }
    @Override public TournamentsApiAuth tournaments() { return tournamentsHandler; }
}
