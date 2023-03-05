package chariot;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.function.*;
import java.util.prefs.Preferences;

import chariot.api.*;
import chariot.api.Builders.*;
import chariot.internal.*;

/**
 * Provides access to the <a href="https://lichess.org/api">Lichess API</a>.
 *
 * <p>
 * There are two types of factory methods to create basic non-authenticated clients, {@link chariot.Client}, and authenticated clients, {@link chariot.ClientAuth}.
 *
 * <p>
 * Examples of how to create a client
 *
 * <p>
 * For accessing non-authenticated parts of the <a href="https://lichess.org/api">Lichess API</a>:
 *
 * {@snippet :
 *     Client client = Client.basic();
 *
 *     // Tada!
 *
 *     // And then use it...
 *     var user = client.users().byId("lichess");
 * }
 *
 * <p>
 * For accessing authenticated parts of the <a href="https://lichess.org/api">Lichess API</a>:<br>
 * <i>
 * Note, a valid token must be acquired in order to use the endpoints exposed by
 * {@link chariot.ClientAuth}, either by obtaining a <a
 * href="https://lichess.org/account/oauth/token">Personal Access Token</a> or by
 * integrating a <a href="https://oauth.net/2/pkce/">PKCE Authorization Code flow</a> in the application - see {@link #auth(Consumer, Consumer)}/{@link #withPkce} for more information.
 * </i>
 *
 * {@snippet :
 *     var token = ... // Token with scope email:read
 *     ClientAuth client = Client.auth(token);
 *
 *     var email = client.account().emailAddress();
 * }
 *
 * <p>
 * The responses from the APIs are modelled with {@link chariot.model.One}{@literal <T>} and {@link chariot.model.Many}{@literal <T>} "containers".<br>
 * Its documentation covers the types of responses and various ways of accessing their values.<br>
 * The types of the values (T) used in the APIs are bundled in the {@link chariot.model} package - simple data holders / records.
 *
 * <p>
 * An additional way of creating a client is via the
 * {@link #load(Preferences) load(prefs)}/{@link #store(Preferences) store(prefs)} methods, keeping
 * the configuration in {@link Preferences},
 * {@snippet :
 *     Preferences prefs = Preferences.userRoot().node("myprefs");
 *     Client client = Client.load(prefs);
 *     if (client instanceof ClientAuth clientAuth) {
 *         // We've managed to restore our previously stored configuration,
 *         // so let's get right down to business.
 *         clientAuth.account().setKidModeStatus(true); // lol
 *     } else {
 *         // It seems this was the first time we ran this program,
 *         // we have yet to store its configuration.
 *         // Let's perform the heavy-lifting now.
 *         var urlToken = client.account().oauthPKCE(Scope.preference_write);
 *         System.out.println("Please grant this application access at Lichess: " + urlToken.url());
 *
 *         // Wait for the user to go to Lichess and click Grant. (They wouldn't click Deny, right?)
 *         var token = urlToken.token().get();
 *
 *         var clientAuth = Client.auth(token);
 *
 *         // Now we can store the configuration for quick access next time.
 *         clientAuth.store(prefs);
 *
 *         // Oh yeah, and...
 *         clientAuth.account().setKidModeStatus(true); // lol
 *     }
 * }
 *
 * <p>
 * There are also customizable variants,
 * {@snippet :
 *     // http://localhost:9663
 *     var client = Client.basic(c -> c.local());
 *
 *     var clientAuth = Client.auth(c -> c.api("https://lichess.dev"), "mytoken");
 * }
 */
public sealed interface Client permits ClientAuth, Client.Basic {

    sealed interface Basic extends Client permits chariot.internal.BasicClient {}

    /**
     * Creates a default client
     */
    static Client basic() {
        return basic(Config.basic(c -> c.production()));
    }

    /**
     * Creates a default client using the provided token to use the authenticated parts of the API
     * @param token A token to use for the authenticated parts of the API
     */
    static ClientAuth auth(String token) {
        return auth(c -> c.production().auth(token));
    }


    /**
     * Helps to perform a OAuth 2 PKCE flow or prepare a URL to create a Personal API Token with specified Scope/s.
     */
    Account account();

    /**
     * Access Lichess cloud evaluations database.
     */
    Analysis analysis();

    /**
     * Access Lichess online bots.<br/>
     * For more bot operations, see {@link chariot.ClientAuth#bot}
     */
    Bot bot();

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
    Broadcasts broadcasts();

    /**
     * Open-ended challenges. For authenticated challenges, see {@link chariot.api.ChallengesAuth}
     */
    Challenges challenges();

    /**
     * External engine. For engine management, see {@link chariot.api.ExternalEngineAuth}
     */
    ExternalEngine externalEngine();

    /**
     * Access games and TV channels, played on Lichess.
     */
    Games games();

    /**
     * Lookup positions from the Lichess opening explorer.
     */
    OpeningExplorer openingExplorer();

    /**
     * Access Lichess puzzle history and dashboard.
     */
    Puzzles puzzles();

    /**
     * Access simuls played on Lichess.
     */
    Simuls simuls();

    /**
     * Access Lichess studies.
     */
    Studies studies();

    /**
     * Lookup positions from the Lichess tablebase server.
     */
    Tablebase tablebase();

    /**
     * Access and manage Lichess teams and their members.
     */
    Teams teams();

    /**
     * Access Arena and Swiss tournaments played on Lichess.<br/>
     */
    Tournaments tournaments();

    /**
     * Access registered users on Lichess.
     */
    Users users();


    /**
     * Creates a customized client
     * @param params A configuration parameters builder
     */
    static Client basic(Consumer<Builder> params){
        return basic(Config.basic(params));
    }
    /**
     * Use a pre-created Personal Access Token to use the authenticated API
     * {@snippet :
     *      String token = ...
     *      Client basic = Client.basic();
     *      ClientAuth auth = basic.withToken(token);
     *
     *      var challengeResult = auth.challenges().challenge(...);
     * }
     * @param token pre-created Personal Access Token - @see
     *              <a href="https://lichess.org/account/oauth/token">Personal Access Token</a>
      */
    default ClientAuth withToken(String token) {
        return withToken(token::toCharArray);
    }

    /**
     * Use a pre-created Personal Access Token to use the authenticated API
     * {@snippet :
     *      Supplier<char[]> token = ...
     *      Client basic = Client.basic();
     *      ClientAuth auth = basic.withToken(token);
     *
     *      var challengeResult = auth.challenges().challenge(...);
     * }
     *
     * @param token pre-created Personal Access Token - @see
     *              <a href="https://lichess.org/account/oauth/token">Personal Access Token</a>
     */
    default ClientAuth withToken(Supplier<char[]> token) {
        if (this instanceof chariot.internal.DefaultClient dc) {
            Config config = dc.config();
            Config.Basic basic = config instanceof Config.Auth auth ? auth.basic() : (Config.Basic) config;
            return Client.auth(basic.withAuth(token));
        }
        return null;
    }

    /**
     * @deprecated Use {@link #auth(Consumer, Supplier)} instead<br>
     *
     * Creates a customizable client using the provided configuration parameters builder.<br>
     * Note, make sure to supply a token using the withToken* methods, or a IllegalArgumentException will be thrown.
     * @param params A configuration parameters builder
     */
    @Deprecated
    static ClientAuth auth(Consumer<TokenBuilder> params) {
        return auth(Config.auth(params));
    }

    /**
     * Creates a customizable client using the provided configuration parameters builder.<br>
     * @param params A configuration parameters builder
     */
    static ClientAuth auth(Consumer<Builder> params, String token) {
        return auth(params, token::toCharArray);
    }

    /**
     * Creates a customizable client using the provided configuration parameters builder.<br>
     * @param params A configuration parameters builder
     */
    static ClientAuth auth(Consumer<Builder> params, Supplier<char[]> token) {
        return basic(params).withToken(token);
    }

    /**
     * Creates a default client using the provided token to use the authenticated parts of the API
     * @param token A token to use for the authenticated parts of the API
     */
    static ClientAuth auth(Supplier<char[]> token) {
        return auth(c -> c.production().auth(token));
    }

    sealed interface AuthResult {}
    record AuthOk(ClientAuth client) implements AuthResult {}
    record AuthFail(String message)  implements AuthResult {}
    record CodeAndState(String code, String state) {}

    interface PkceConfig {

        /**
         * @param scopes The scope/s, if any, that the resulting token should be valid for.
         */
        PkceConfig scope(Scope... scopes);

        /**
         * @param timeout How long to wait for user to grant access. Default 2 minutes.
         */
        PkceConfig timeout(Duration timeout);
        /**
         * @param timeoutSeconds How long to wait for user to grant access. Default 2 minutes.
         */
        default PkceConfig timeoutSeconds(long timeoutSeconds) { return timeout(Duration.ofSeconds(timeoutSeconds)); }

        /**
         * @param html If you want to customize the contents of the HTML page which the user is redireced to after granting access.<br/>
         *             The default page says success and links to the security preferences page where the user can revoke the token.
         */
        PkceConfig htmlSuccess(String html);

        /**
         *
         * By default the PKCE flow starts a local HTTP server on 127.0.0.1 to where
         * Lichess redirects the user when they grant access.
         * The local HTTP server listens for the incoming redirect and parses the
         * {@code code} and {@code state} parameters sent by Lichess.<br/>
         * But in case your application is running on a public HTTP(S) server,
         * the PKCE flow should redirect the user to the public site.<br/>
         * This method is used to provide that custom redirect URL.
         *
         * @param redirectUri  To where Lichess should redirect the user grant response,
         *                     which includes the {@code code} and {@code state}
         *                     parameters if the user granted access.
         * @param codeAndState In order for chariot to be able to complete the PKCE
         *                     flow, it will need the {@code code} and {@code state}
         *                     parameters which were sent to the redirect URL.
         *                     This is the supplier you use for those parameters
         *                     when you've received them.
         */
        PkceConfig customRedirect(URI redirectUri, Supplier<CodeAndState> codeAndState);
    }

    /**
     * Use OAuth PKCE flow to make it possible for your user to grant access to your application.
     * {@snippet :
     *      Client basic = Chariot.basic();
     *
     *      AuthResult authResult = basic.withPkce(
     *          uri -> System.out.format("Visit %s to review and grant access%n", uri),
     *          pkce -> pkce.scope(Scope.challenge_read, Scope.challenge_write));
     *
     *      if (! (authResult instanceof AuthOk ok)) return;
     *
     *      ClientAuth auth = ok.client();
     *      var challengeResult = auth.challenges().challenge(...);
     * }
     * @param uriHandler The generated Lichess URI that your user can visit to review and approve granting access to your application
     * @param pkce Configuration of for instance which scopes if any that the resulting Access Token should include.
     */
    default AuthResult withPkce(Consumer<URI> uriHandler, Consumer<PkceConfig> pkce) {
        return PKCE.pkceAuth(this, uriHandler, pkce);
    }

    /**
     * Use OAuth PKCE flow to make it possible for your user to grant access to your application.
     * {@snippet :
     *      AuthResult authResult = Client.auth(
     *          uri -> System.out.format("Visit %s to review and grant access%n", uri),
     *          pkce -> pkce.scope(Scope.challenge_read, Scope.challenge_write));
     *
     *      if (! (authResult instanceof AuthOk ok)) return;
     *
     *      ClientAuth auth = ok.client();
     *      var challengeResult = auth.challenges().challenge(...);
     * }
     * @param uriHandler The generated Lichess URI that your user can visit to review and approve granting access to your application
     * @param pkce Configuration of for instance which scopes if any that the resulting Access Token should include.
     */
    static AuthResult auth(Consumer<URI> uriHandler, Consumer<PkceConfig> pkce) {
        return auth(c -> c.production(), uriHandler, pkce);
    }

    /**
     * Use OAuth PKCE flow to make it possible for your user to grant access to your application.
     * {@snippet :
     *      AuthResult authResult = Client.auth(
     *          conf -> conf.api("http://localhost:9663"),
     *          uri -> System.out.format("Visit %s to review and grant access%n", uri),
     *          pkce -> pkce.scope(Scope.challenge_read, Scope.challenge_write));
     *
     *      if (! (authResult instanceof AuthOk ok)) return;
     *
     *      ClientAuth auth = ok.client();
     *      var challengeResult = auth.challenges().challenge(...);
     * }
     * @param config Customized client configuration such as enabling logging and number of retries etc.
     * @param uriHandler The generated Lichess URI that your user can visit to review and approve granting access to your application
     * @param pkce Configuration of for instance which scopes if any that the resulting Access Token should include.
     */
    static AuthResult auth(Consumer<Builder> config, Consumer<URI> uriHandler, Consumer<PkceConfig> pkce) {
        return basic(config).withPkce(uriHandler, pkce);
    }

    /**
     * Creates a customized client from a preferences node<br>
     * See {@link Client#store(Preferences)}
     * @param prefs A configuration preferences node<br>
     * {@code if (client instanceof ClientAuth auth) ...}
     */
    static Client load(Preferences prefs) {
        return load(Config.load(prefs));
    }

    /**
     * Stores the client configuration into the provided preferences node<br>
     * See {@link Client#load(Preferences)}
     * @param prefs The preferences node to store this client configuration to
     */
    boolean store(Preferences prefs);

    /**
     * Clears client token information from preferences.<br>
     * See {@link Client#load(Preferences)}
     * @param prefs The preferences node to clear
     */
    default void clearAuth(Preferences prefs) { Config.clearAuth(prefs); }

    /**
     * Creates an authenticated customized client from a preferences node with provided token<br>
     * @param prefs A configuration preferences node
     * @param token A token to use for the authenticated parts of the API
     */
    static ClientAuth load(Preferences prefs, Consumer<AuthBuilder> token) {
        return auth(Config.load(prefs, token));
    }

    /**
     * Helper method for initializing and storing an authorized client
     * configuration.<br>
     * If the provided {@link Preferences} node already contains the token for the
     * needed scopes, the authorized client will be returned immediately. If it
     * doesn't contain such a token, the Lichess URL for a page to authorize
     * the scopes will be written to standard output, for the user to grant
     * access. If access is granted, the authorized client configuration will
     * be stored in the Preferences node, and the authorized client is
     * returned.<br>
     * The implementation of this helper method does this:
     * {@snippet :
     *   Preferences prefsNode = Preferences.userRoot().node(prefs);
     *   var client = Client.load(prefsNode);
     *   if (client instanceof ClientAuth auth &&
     *       auth.account().scopes().containsAll(Set.of(scopes)))
     *       return auth;
     *
     *   var oauth = client.account().oauthPKCE(scopes);
     *   System.out.println("\n\nGrant access at URL:\n" + oauth.url() + "\n");
     *   var clientAuth = Client.load(prefs, auth -> auth.auth(oauth.token().get()));
     *   clientAuth.store(prefs);
     *
     *   return clientAuth;
     * }
     *
     * @param params a functional interface with configuration, i.e {@code Client.reuseOrInitialize(p -> p.prefs("my-prefs").scopes(Scope.team_read))}
     * @return a client authorized for the requested scope/s
     */
    static ClientAuth reuseOrInitialize(Consumer<OAuthHelper> params) {
        var helper = new OAuthHelper() {
            Preferences preferences = null;
            Scope[] scopes = null;
            @Override
            public OAuthHelper prefs(Preferences preferences) {
                this.preferences = preferences;
                return this;
            }
            @Override
            public OAuthHelper scopes(Scope... scopes) {
                Objects.requireNonNull(scopes);
                this.scopes = scopes;
                return this;
            }
        };
        params.accept(helper);
        Objects.requireNonNull(helper.preferences);
        return reuseOrInitialize(helper.preferences, helper.scopes);
    }

    /**
     * See {@link Client#reuseOrInitialize(Consumer)}
     */
    static ClientAuth reuseOrInitialize(Preferences prefs, Scope... scopes) {
        var client = Client.load(prefs);
        if (client instanceof ClientAuth auth &&
            auth.account().scopes().containsAll(Set.of(scopes)))
            return auth;

        @SuppressWarnings(value = {"deprecation"})
        var oauth = client.account().oauthPKCE(scopes);
        System.out.println("\n\nGrant access at URL:\n" + oauth.url() + "\n");
        var clientAuth = Client.load(prefs, auth -> auth.auth(oauth.token().get()));
        clientAuth.store(prefs);

        return clientAuth;
    }

    /**
     * Retrieves an Optional containing a {@code ClientAuth} if this is such a client, otherwise empty.
     */
    default Optional<ClientAuth> asAuth() {
        return this instanceof chariot.internal.AuthClient auth ? Optional.of(auth) : Optional.empty();
    }

    /**
     * Configure logging levels
     */
    default void logging(Consumer<LogSetter> params) {
        var logging = ((chariot.internal.DefaultClient) this).config().logging();
        var builder = new Config.LBuilderImpl(logging);
        params.accept(builder);
    }

    private static Client load(Config config) {
        // --enable-preview
        //return switch(config) {
        //    case Config.Auth auth -> new AuthClient(auth);
        //    case Config.Basic basic -> new BasicClient(basic);
        //};

        if (config instanceof Config.Basic b) {
            return new chariot.internal.BasicClient(b);
        } else if (config instanceof Config.Auth a) {
            return new chariot.internal.AuthClient(a);
        }
        throw new RuntimeException("Unknown config type: " + config);
    }

    private static Client basic(Config.Basic config) {
        return new chariot.internal.BasicClient(config);
    }

    private static ClientAuth auth(Config.Auth config) {
        return new chariot.internal.AuthClient(config);
    }

    Custom custom();

    /**
     * OAuth scopes representing different permissions
     */
    public enum Scope {
        /**
         * Read your preferences
         */
        preference_read,

        /**
         * Write your preferences
         */
        preference_write,

        /**
         * Read your email address
         */
        email_read,

        /**
         * Read incoming challenges
         */
        challenge_read,

        /**
         * Create, accept, decline challenges
         */
        challenge_write,

        /**
         * Create, delete, query bulk pairings
         */
        challenge_bulk,

        /**
         * Read private studies and broadcasts
         */
        study_read,

        /**
         * Create, update, delete studies and broadcasts
         */
        study_write,

        /**
         * Create tournaments
         */
        tournament_write,

        /**
         * Create and join puzzle races
         */
        racer_write,

        /**
         * Read puzzle activity
         */
        puzzle_read,

        /**
         * Read private team information
         */
        team_read,

        /**
         * Join, leave
         */
        team_write,

        /**
         * Manage teams (kick members, send team messages, accept/decline join requests)
         */
        team_lead,

        /**
         * Send private messages to other players
         */
        msg_write,

        /**
         * Play with the Board API
         */
        board_play,

        /**
         * Play with the Bot API. Only for Bot accounts
         */
        bot_play,

        /**
         * Read followed players
         */
        follow_read,

        /**
         * Follow and Unfollow players
         */
        follow_write,

        /**
         * View and use your external engines
         */
        engine_read,

        /**
         * Create and update external engines
         */
        engine_write,

        /**
         * Create authenticated website sessions (grants full access!)
         */
        web_login,

        web_mod,

        any;

        public String asString() {
            return switch (this) {
                case any -> "*";
                default  -> name().replace("_", ":");
            };
        }

        public static Optional<Scope> fromString(String scope) {
            try {
                // preferences:read -> preferences_read
                return Optional.of(valueOf(scope.replace(":", "_")));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }
}
