package chariot.internal;

import java.net.URI;
import java.util.*;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import chariot.Client.Scope;
import chariot.api.*;
import chariot.internal.impl.*;
import chariot.model.*;

public abstract class ClientBase {

    public final InternalClient client;
    private final Config config;

    public RequestHandler requestHandler() {
        return client::request;
    }

    protected final AnalysisHandler analysisHandler;
    protected final BotHandler botHandler;
    protected final BroadcastsHandler broadcastsHandler;
    protected final ChallengesHandler challengesHandler;
    protected final ExternalEngineHandler externalEngineHandler;
    protected final GamesHandler gamesHandler;
    protected final OpeningExplorerHandler openingExplorerHandler;
    protected final PuzzlesHandler puzzlesHandler;
    protected final SimulsHandler simulsHandler;
    protected final StudiesHandler studiesHandler;
    protected final TablebaseHandler tablebaseHandler;
    protected final TeamsHandler teamsHandler;
    protected final TokenHandler tokenHandler;
    protected final TournamentsHandler tournamentsHandler;
    protected final UsersHandler usersHandler;
    protected final CustomHandler customHandler;

    protected Config config() {
        return config;
    }

    public ClientBase(Config config) {
        this.config = config;
        client = new InternalClient(config);

        analysisHandler = new AnalysisHandler(client::request);
        botHandler = new BotHandler(client::request);
        broadcastsHandler = new BroadcastsHandler(client::request);
        challengesHandler = new ChallengesHandler(client::request);
        externalEngineHandler = new ExternalEngineHandler(client::request);
        gamesHandler = new GamesHandler(client::request);
        openingExplorerHandler = new OpeningExplorerHandler(client::request);
        puzzlesHandler = new PuzzlesHandler(client::request);
        simulsHandler = new SimulsHandler(client::request);
        studiesHandler = new StudiesHandler(client, client::request);
        tablebaseHandler = new TablebaseHandler(client::request);
        teamsHandler = new TeamsHandler(client::request);
        tokenHandler = new TokenHandler(client, requestHandler());
        tournamentsHandler = new TournamentsHandler(client::request);
        usersHandler = new UsersHandler(client::request);
        customHandler = new CustomHandler(client::request);
    }

    /**
     * Access Lichess cloud evaluations database.
     */
    public Analysis analysis() { return analysisHandler; }

    /**
     * Access Lichess online bots.<br/>
     * For more bot operations, see {@link chariot.ClientAuth#bot}
     */
    public Bot bot() { return botHandler; }


    /**
     * Relay chess events on Lichess.
     * <p>Official broadcasts are maintained by Lichess, but you can create your own
     * broadcasts to cover any live game or chess event. You will need to publish
     * PGN on a public URL so that Lichess can pull updates from it.
     * Alternatively, you can push PGN updates to Lichess using this API.
     * <p>Broadcasts are organized in tournaments, which have several rounds, which
     * have several games. You must first create a tournament, then you can add
     * rounds to them.
     */
    public Broadcasts broadcasts() { return broadcastsHandler; }

    /**
     * Open-ended challenges. For authenticated challenges, see {@link chariot.api.ChallengesAuth}
     */
    public Challenges challenges() { return challengesHandler; }

    /**
     * External engine. For engine management, see {@link chariot.api.ExternalEngineAuth}
     */
    public ExternalEngine externalEngine() { return externalEngineHandler; }

    /**
     * Access games and TV channels, played on Lichess.
     */
    public Games games() { return gamesHandler; }

    /**
     * Lookup positions from the Lichess opening explorer.
     */
    public OpeningExplorer openingExplorer() { return openingExplorerHandler; }

    /**
     * Access Lichess puzzle history and dashboard.
     */
    public Puzzles puzzles() { return puzzlesHandler;}

    /**
     * Access simuls played on Lichess.
     */
    public Simuls simuls() { return simulsHandler; }

    /**
     * Access Lichess studies.
     */
    public Studies studies() { return studiesHandler; }

    /**
     * Lookup positions from the Lichess tablebase server.
     */
    public Tablebase tablebase() { return tablebaseHandler; }

    /**
     * Access and manage Lichess teams and their members.
     */
    public Teams teams() { return teamsHandler; }

    /**
     * Access Arena and Swiss tournaments played on Lichess.<br/>
     */
    public Tournaments tournaments() { return tournamentsHandler; }


    /**
     * Use chariot for custom endpoints
     */
    public Custom custom() { return customHandler; }

    //* See {@link Client#load(Preferences)}

    /**
     * Stores the client configuration into the provided preferences node<br>
     * @param prefs The preferences node to store this client configuration to
     */
    public boolean store(Preferences prefs) {
        config.store(prefs);
        try {
            prefs.flush();
            return true;
        } catch(Exception e) { e.printStackTrace(System.err); }
        return false;
    }



     //* <p>See also {@link #withPkce(Consumer, Consumer)}

    /**
     * Helper method for creating <a href="https://lichess.org/account/oauth/token">Personal Access Tokens</a>
     * <p>Note, a user must create the token manually.
     * {@snippet :
     * Client client = Client.basic();
     * var url = client.personalAccessTokenForm("Token for reading preferences", Scope.preferences_read);
     *
     * System.out.println("Please create a token at Lichess,\nusing this pre-filled form: " + url);
     * System.out.println("Copy and paste the token when done");
     *
     * // Wait for the token
     * String token = System.console().readLine();
     *
     * ClientAuth clientAuth = Client.auth(token);
     * var email = clientAuth.account().emailAddress();
     * }
     * @param description A description for the intended use of the token, so the user can easier recognize the token when browsing their security information.
     * @param scopes The pre-selected scopes for the token
     * @return A URL where user can create a token, in order to then copy the token and use as input to an application.<br>
     * Example: https://lichess.org/account/oauth/token/create?scopes[]=challenge:write{@literal &}scopes[]=puzzle:read{@literal &}description=Prefilled+token+example
     */

    public URI personalAccessTokenForm(String description, Scope... scopes) {
        return tokenHandler.personalAccessTokenForm(description, scopes);
    }
    public One<TokenBulkResult> testTokens(Set<String> tokens) {
        return tokenHandler.testTokens(tokens);
    }

    /**
     * Read which scopes are available with a token
     * @param token
     */
    public Set<Scope> scopes(Supplier<char[]> token) {
        return tokenHandler.scopes(token);
    }
    public TokenResult token(Map<String, String> parameters) {
        return tokenHandler.token(parameters);
    }
    public One<TokenBulkResult> testTokens(String... tokens) { return testTokens(Set.of(tokens)); }
    public Set<Scope> scopes(String token) { return scopes(() -> token.toCharArray()); }

}
