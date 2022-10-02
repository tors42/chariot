package chariot.internal;

import java.util.prefs.Preferences;

import chariot.internal.Config.TokenType;
import chariot.api.*;
import chariot.internal.impl.*;

public class DefaultClient {

    private final InternalClient client;
    private final Config config;

    private final AccountAuthImpl account;
    private final AdminAuthImpl admin;
    private final AnalysisImpl analysis;
    private final BoardAuthImpl board;
    private final BotAuthImpl bot;
    private final BroadcastsAuthImpl broadcasts;
    private final ChallengesAuthImpl challenges;
    private final CustomImpl custom;
    private final GamesAuthImpl games;
    private final OpeningExplorerImpl openingExplorer;
    private final PuzzlesAuthImpl puzzles;
    private final SimulsImpl simuls;
    private final StudiesImpl studies;
    private final TablebaseImpl tablebase;
    private final TeamsAuthImpl teams;
    private final TournamentsAuthImpl tournaments;
    private final UsersAuthImpl users;

    public DefaultClient(final Config config) {
        this.config = config;
        client = new InternalClient(config);

        account = new AccountAuthImpl(client);

        if (config instanceof Config.Auth auth) {
            if (auth.type() instanceof TokenType.AutoScopedTokens auto) {
                auto.resolve(account::scopes);
            }
        }

        admin = new AdminAuthImpl(client);
        analysis = new AnalysisImpl(client);
        challenges = new ChallengesAuthImpl(client);
        custom = new CustomImpl(client);
        board = new BoardAuthImpl(client);
        bot = new BotAuthImpl(client);
        broadcasts = new BroadcastsAuthImpl(client);
        games = new GamesAuthImpl(client);
        openingExplorer = new OpeningExplorerImpl(client);
        puzzles = new PuzzlesAuthImpl(client);
        simuls = new SimulsImpl(client);
        studies = new StudiesImpl(client);
        tablebase = new TablebaseImpl(client);
        teams = new TeamsAuthImpl(client);
        tournaments = new TournamentsAuthImpl(client);
        users = new UsersAuthImpl(client);
    }

    public AccountAuth account() {
        return account;
    }

    public AdminAuth admin() {
        return admin;
    }

    public Analysis analysis() {
        return analysis;
    }

    public BoardAuth board() {
        return board;
    }

    public BotAuth bot() {
        return bot;
    }

    public BroadcastsAuth broadcasts() {
        return broadcasts;
    }

    public ChallengesAuth challenges() {
        return challenges;
    }

    public Custom custom() {
        return custom;
    }

    public GamesAuth games() {
        return games;
    }

    public OpeningExplorer openingExplorer() {
        return openingExplorer;
    }

    public PuzzlesAuth puzzles() {
        return puzzles;
    }

    public Simuls simuls() {
        return simuls;
    }

    public Studies studies() {
        return studies;
    }

    public Tablebase tablebase() {
        return tablebase;
    }

    public TeamsAuth teams() {
        return teams;
    }

    public TournamentsAuth tournaments() {
        return tournaments;
    }

    public UsersAuth users() {
        return users;
    }

    public Config config() {
        return config;
    }

    public boolean store(Preferences prefs) {
        config.store(prefs);
        try {
            prefs.flush();
            return true;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
