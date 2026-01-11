package chariot.internal.impl;

import module java.base;
import module chariot;

import chariot.internal.Config;
import chariot.internal.InternalClient;
import chariot.internal.RequestHandler;
import chariot.util.OAuth;

public class ClientImpl implements Client {

    RequestHandler requestHandler() {
        return client::request;
    }

    final InternalClient client;
    final Config config;

    final AnalysisHandler analysisHandler;
    final BotHandler botHandler;
    final BroadcastsHandler broadcastsHandler;
    final ChallengesHandler challengesHandler;
    final ExternalEngineHandler externalEngineHandler;
    final FideApiHandler fideApiHandler;
    final GamesHandler gamesHandler;
    final OpeningExplorerHandler openingExplorerHandler;
    final PuzzlesHandler puzzlesHandler;
    final SimulsHandler simulsHandler;
    final StudiesHandler studiesHandler;
    final TablebaseHandler tablebaseHandler;
    final TeamsHandler teamsHandler;
    final TournamentsHandler tournamentsHandler;
    final UsersHandler usersHandler;
    final OAuthHandler oAuthHandler;
    final CustomHandler customHandler;

    public ClientImpl(Config config) {
        this.config = config;
        client = new InternalClient(config);

        analysisHandler = new AnalysisHandler(client::request);
        botHandler = new BotHandler(client::request);
        broadcastsHandler = new BroadcastsHandler(client::request);
        challengesHandler = new ChallengesHandler(client::request);
        externalEngineHandler = new ExternalEngineHandler(client::request);
        fideApiHandler = new FideApiHandler(client::request);
        gamesHandler = new GamesHandler(client::request);
        openingExplorerHandler = new OpeningExplorerHandler(client::request);
        puzzlesHandler = new PuzzlesHandler(client::request);
        simulsHandler = new SimulsHandler(client::request);
        studiesHandler = new StudiesHandler(client, client::request);
        tablebaseHandler = new TablebaseHandler(client::request);
        teamsHandler = new TeamsHandler(client::request);
        tournamentsHandler = new TournamentsHandler(client::request);
        usersHandler = new UsersHandler(client::request);
        oAuthHandler = new OAuthHandler(client, requestHandler());
        customHandler = new CustomHandler(client::request);
    }

    public Config config() {
        return config;
    }

    @Override
    public AnalysisApi analysis() { return analysisHandler; }
    @Override
    public BotApi bot() { return botHandler; }
    @Override
    public BroadcastsApi broadcasts() { return broadcastsHandler; }
    @Override
    public ChallengesApi challenges() { return challengesHandler; }
    @Override
    public ExternalEngineApi externalEngine() { return externalEngineHandler; }
    @Override
    public FideApi fide() { return fideApiHandler; }
    @Override
    public GamesApi games() { return gamesHandler; }
    @Override
    public OpeningExplorerApi openingExplorer() { return openingExplorerHandler; }
    @Override
    public PuzzlesApi puzzles() { return puzzlesHandler;}
    @Override
    public SimulsApi simuls() { return simulsHandler; }
    @Override
    public StudiesApi studies() { return studiesHandler; }
    @Override
    public TablebaseApi tablebase() { return tablebaseHandler; }
    @Override
    public TeamsApi teams() { return teamsHandler; }
    @Override
    public TournamentsApi tournaments() { return tournamentsHandler; }
    @Override
    public UsersApi users() { return usersHandler; }

    @Override
    public OAuthApi oauth() { return oAuthHandler; }
    @Override
    public CustomApi custom() { return customHandler; }

    @Override
    public boolean store(Preferences prefs) {
        config.store(prefs);
        try {
            prefs.flush();
            return true;
        } catch(Exception e) { e.printStackTrace(System.err); }
        return false;
    }

    @Override
    public void logging(Consumer<Builders.LoggingBuilder> params) {
        var logging = config().logging();
        var builder = Config.loggingBuilder(logging);
        params.accept(builder);
    }

    @Override
    public One<ClientAuth> withPkce(Consumer<URI> uriHandler, Consumer<PkceConfig> pkce) {
        return OAuth.lichessAuthorizationCodeFlowPKCE(uriHandler, pkce, this)
            .mapOne(this::withToken);
    }

    @Override
    public ClientAuth withToken(Supplier<char[]> token) {
        var config = config().withToken(token);
        return new chariot.internal.impl.ClientAuthImpl(config);
    }

}
