package chariot.internal;

import java.net.URI;
import java.util.*;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import chariot.ClientAuth;
import chariot.api.*;
import chariot.internal.impl.*;
import chariot.model.One;
import chariot.model.*;

public record Default(Config config, InternalClient client, Apis apis) implements ClientAuth {

    public static Default of(Config config) {
        var client = new InternalClient(config);
        var apis = Apis.of(client);
        return new Default(config, client, apis);
    }

    record Apis(
            AccountAuth account,
            UsersAuth users,
            AdminAuth admin,
            Analysis analysis,
            BoardAuth board,
            BotAuth bot,
            BroadcastsAuth broadcasts,
            ChallengesAuth challenges,
            ExternalEngineAuth externalEngine,
            GamesAuth games,
            OpeningExplorer openingExplorer,
            PuzzlesAuth puzzles,
            Studies studies,
            Simuls simuls,
            Tablebase tablebase,
            TeamsAuth teams,
            TournamentsAuth tournaments,
            TokenHandler tokenHandler,
            CustomHandler custom) {

        static Apis of(InternalClient client) {
            return new Apis(
                    new AccountHandler(client::request),
                    new UsersHandler(client::request),
                    new AdminHandler(client::request),
                    new AnalysisHandler(client::request),
                    new BoardHandler(client::request),
                    new BotHandler(client::request),
                    new BroadcastsHandler(client::request),
                    new ChallengesHandler(client::request),
                    new ExternalEngineHandler(client::request),
                    new GamesHandler(client::request),
                    new OpeningExplorerHandler(client::request),
                    new PuzzlesHandler(client::request),
                    new StudiesHandler(client, client::request),    // <---- look! look! it doesn't follow the pattern but tries to hide among the others!
                    new SimulsHandler(client::request),
                    new TablebaseHandler(client::request),
                    new TeamsHandler(client::request),
                    new TournamentsHandler(client::request),
                    new TokenHandler(client, client::request),      // <---- look! look! it doesn't follow the pattern but tries to hide among the others!
                    new CustomHandler(client::request));
        }
    }

    @Override public UsersAuth users()                      { return apis().users(); }
    @Override public AccountAuth account()                  { return apis().account(); }
    @Override public AdminAuth admin()                      { return apis().admin(); }
    @Override public Analysis analysis()                    { return apis().analysis(); }
    @Override public BoardAuth board()                      { return apis().board(); }
    @Override public BotAuth bot()                          { return apis().bot(); }
    @Override public BroadcastsAuth broadcasts()            { return apis().broadcasts(); }
    @Override public ChallengesAuth challenges()            { return apis().challenges(); }
    @Override public ExternalEngineAuth externalEngine()    { return apis().externalEngine(); }
    @Override public GamesAuth games()                      { return apis().games(); }
    @Override public OpeningExplorer openingExplorer()      { return apis().openingExplorer(); }
    @Override public PuzzlesAuth puzzles()                  { return apis().puzzles(); }
    @Override public Studies studies()                      { return apis().studies(); }
    @Override public Simuls simuls()                        { return apis().simuls(); }
    @Override public Tablebase tablebase()                  { return apis().tablebase(); }
    @Override public TeamsAuth teams()                      { return apis().teams(); }
    @Override public TournamentsAuth tournaments()          { return apis().tournaments(); }
    @Override public Custom custom()                        { return apis().custom(); }

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
    public URI personalAccessTokenForm(String description, Scope... scopes) {
        return apis().tokenHandler().personalAccessTokenForm(description, scopes);
    }

    @Override
    public One<TokenBulkResult> testTokens(Set<String> tokens) {
        return apis().tokenHandler().testTokens(tokens);
    }

    @Override
    public Set<Scope> scopes(Supplier<char[]> token) {
        return apis().tokenHandler().scopes(token);
    }

    @Override
    public Collection<Scope> scopes() {
        return apis().tokenHandler().scopes();
    }

    @Override
    public One<Void> revokeToken() {
        return apis().tokenHandler().revokeToken();
    }

    public TokenResult token(Map<String, String> parameters) {
        return apis().tokenHandler().token(parameters);
    }

    @Override
    public String toString() {
        return String.valueOf(config());
    }
}
