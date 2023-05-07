package chariot;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.function.*;
import java.util.prefs.Preferences;

import chariot.api.*;
import chariot.api.Builders.*;
import chariot.internal.*;
import chariot.model.*;

/**
 * Provides access to the <a href="https://lichess.org/api">Lichess API</a>.
 *
 * <p>
 * This interface provides access to the public API of Lichess (which doesn't
 * need a Lichess account.)<br>
 * The {@link chariot.ClientAuth} interface provides access to the authenticated
 * API of Lichess (which needs a Lichess account.)<br>
 *
 * <p>
 * Example of how to get a reference to a {@link chariot.Client} interface
 * <p>
 * {@snippet :
 * Client client = Client.basic();
 *
 * // Tada!
 *
 * // And then use it...
 * var user = client.users().byId("lichess");
 * }
 * <p>
 * For accessing authenticated parts of the API, one needs a token with
 * appropriate access rights / scopes. This library support PKCE Authorization
 * Code flow ({@link #auth(Consumer, Consumer)}), but one can also create a
 * <a href="https://lichess.org/account/oauth/token">Personal Access Token</a>
 * manually.
 * <p>
 * Example of how to get a reference to a {@link chariot.ClientAuth} interface
 * <p>
 * {@snippet :
 * String token = ... // Token with scope email:read
 * ClientAuth client = Client.auth(token);
 *
 * // Tada!
 *
 * // And then use it...
 * var email = client.account().emailAddress();
 * }
 * <p>
 * The responses from the API are modelled with
 * {@link chariot.model.One}{@literal <T>} and
 * {@link chariot.model.Many}{@literal <T>} "containers".<br>
 * Their documentation covers different ways of accessing the contents of the
 * containers.<br>
 * The contents of the containers are typically data models from the
 * {@link chariot.model} package.
 */
public sealed interface Client permits ClientAuth {

    /**
     * Creates a default client
     */
    static Client basic() {
        return basic(Config.of());
    }

    /**
     * Access registered users on Lichess.
     */
     <T extends User> Users<T> users();

    /**
     * Access Lichess cloud evaluations database.
     */
    chariot.api.Analysis analysis();

    /**
     * Access Lichess online bots.<br/>
     * For more bot operations, see {@link chariot.ClientAuth#bot}
     */
    <T extends User> Bot<T> bot();

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
    chariot.api.Simuls simuls();

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
    <T extends TeamMember> Teams<T> teams();

    /**
     * Access Arena and Swiss tournaments played on Lichess.<br/>
     */
    Tournaments tournaments();


    /**
     * Use chariot for custom endpoints
     */
    Custom custom();


    /**
     * Creates a default client using the provided token to use the authenticated parts of the API
     * @param token A token to use for the authenticated parts of the API
     */
    static ClientAuth auth(String token) {
        return auth(token::toCharArray);
    }

    /**
     * Creates a customized client
     * @param params A configuration parameters builder
     */
    static Client basic(Consumer<ConfigBuilder> params){
        return basic(Config.basic(params));
    }
    /**
     * Use a pre-created Personal Access Token to use the authenticated API
     * {@snippet :
     * String token = ...
     * Client basic = Client.basic();
     * ClientAuth auth = basic.withToken(token);
     *
     * var challengeResult = auth.challenges().challenge(...);
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
     * Supplier<char[]> token = ...
     * Client basic = Client.basic();
     * ClientAuth auth = basic.withToken(token);
     *
     * var challengeResult = auth.challenges().challenge(...);
     * }
     *
     * @param token pre-created Personal Access Token - @see
     *              <a href="https://lichess.org/account/oauth/token">Personal Access Token</a>
     */
    default ClientAuth withToken(Supplier<char[]> token) {
        if (!(this instanceof Default current)) return null;
        var config = current.config().withToken(token);
        var d = Default.of(config);
        return d;
    }

    /**
     * Creates a customizable client using the provided configuration parameters builder.<br>
     * @param params A configuration parameters builder
     */
    static ClientAuth auth(Consumer<ConfigBuilder> params, Supplier<char[]> token) { return basic(params).withToken(token); }


    /**
     * Creates a customizable client using the provided configuration parameters builder.<br>
     * @param params A configuration parameters builder
     */
    static ClientAuth auth(Consumer<ConfigBuilder> params, String token) { return auth(params, token::toCharArray); }

    /**
     * Creates a default client using the provided token to use the authenticated parts of the API
     * @param token A token to use for the authenticated parts of the API
     */
    static ClientAuth auth(Supplier<char[]> token) { return auth(c -> {}, token); }

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
     * Client basic = Chariot.basic();
     *
     * AuthResult authResult = basic.withPkce(
     *     uri -> System.out.format("Visit %s to review and grant access%n", uri),
     *     pkce -> pkce.scope(Scope.challenge_read, Scope.challenge_write));
     *
     * if (! (authResult instanceof AuthOk ok)) return;
     *
     * ClientAuth auth = ok.client();
     * var challengeResult = auth.challenges().challenge(...);
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
     * AuthResult authResult = Client.auth(
     *     uri -> System.out.format("Visit %s to review and grant access%n", uri),
     *     pkce -> pkce.scope(Scope.challenge_read, Scope.challenge_write));
     *
     * if (! (authResult instanceof AuthOk ok)) return;
     *
     * ClientAuth auth = ok.client();
     * var challengeResult = auth.challenges().challenge(...);
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
     * AuthResult authResult = Client.auth(
     *     conf -> conf.api("http://localhost:9663"),
     *     uri -> System.out.format("Visit %s to review and grant access%n", uri),
     *     pkce -> pkce.scope(Scope.challenge_read, Scope.challenge_write));
     *
     * if (! (authResult instanceof AuthOk ok)) return;
     *
     * ClientAuth auth = ok.client();
     * var challengeResult = auth.challenges().challenge(...);
     * }
     * @param config Customized client configuration such as enabling logging and number of retries etc.
     * @param uriHandler The generated Lichess URI that your user can visit to review and approve granting access to your application
     * @param pkce Configuration of for instance which scopes if any that the resulting Access Token should include.
     */
    static AuthResult auth(Consumer<ConfigBuilder> config, Consumer<URI> uriHandler, Consumer<PkceConfig> pkce) {
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
    static ClientAuth load(Preferences prefs, String token) {
        return load(prefs).withToken(token);
    }


    /**
     * Retrieves an Optional containing a {@code ClientAuth} if this is such a client, otherwise empty.
     */
    default Optional<ClientAuth> asAuth() {
        return this instanceof Default def && def.config() instanceof Config.Auth a ? Optional.of((ClientAuth)this) : Optional.empty();
    }

    /**
     * Configure logging levels
     */
    default void logging(Consumer<LoggingBuilder> params) {
        var logging = ((Default) this).config().logging();
        var builder = Config.loggingBuilder(logging);
        params.accept(builder);
    }

    private static Client load(Config config) {
        return Default.of(config);
    }

    private static Client basic(Config.Basic config) {
        return Default.of(config);
    }

    /**
     * Helper method for creating <a href="https://lichess.org/account/oauth/token">Personal Access Tokens</a>
     * <p>Note, a user must create the token manually.
     * <p>See also {@link #withPkce(Consumer, Consumer)}
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
    URI personalAccessTokenForm(String description, Scope... scopes);


    One<TokenBulkResult> testTokens(Set<String> tokens);
    default One<TokenBulkResult> testTokens(String... tokens) { return testTokens(Set.of(tokens)); }

    /**
     * Read which scopes are available with a token
     * @param token
     */
    Set<Scope> scopes(Supplier<char[]> token);


    default Set<Scope> scopes(String token) { return scopes(() -> token.toCharArray()); }


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
