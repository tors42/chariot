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
public class Client extends chariot.internal.ClientBase {

    Client(Config config) {
        super(config);
    }

    /** Access registered users on Lichess. */
    public Users users() {
        return super.usersHandler;
    }

    /** {@inheritDoc} */ @Override public Analysis analysis() { return super.analysis(); }
    /** {@inheritDoc} */ @Override public Bot bot() { return super.bot(); }
    /** {@inheritDoc} */ @Override public Broadcasts broadcasts() { return super.broadcasts(); }
    /** {@inheritDoc} */ @Override public Challenges challenges() { return super.challenges(); }
    /** {@inheritDoc} */ @Override public ExternalEngine externalEngine() { return super.externalEngine(); }
    /** {@inheritDoc} */ @Override public Games games() { return super.games(); }
    /** {@inheritDoc} */ @Override public OpeningExplorer openingExplorer() { return super.openingExplorer(); }
    /** {@inheritDoc} */ @Override public Puzzles puzzles() { return super.puzzles();}
    /** {@inheritDoc} */ @Override public Simuls simuls() { return super.simuls(); }
    /** {@inheritDoc} */ @Override public Studies studies() { return super.studies(); }
    /** {@inheritDoc} */ @Override public Tablebase tablebase() { return super.tablebase(); }
    /** {@inheritDoc} */ @Override public Teams teams() { return teamsHandler; }
    /** {@inheritDoc} */ @Override public Tournaments tournaments() { return super.tournaments(); }
    /** {@inheritDoc} */ @Override public Custom custom() { return super.custom(); }



    /** {@inheritDoc} */ @Override public boolean store(Preferences prefs) { return super.store(prefs); }

    /**
     * Creates a default client
     */
    public static Client basic() {
        return basic(Config.of());
    }


    /**
     * Creates a default client using the provided token to use the authenticated parts of the API
     * @param token A token to use for the authenticated parts of the API
     */
    public static ClientAuth auth(String token) {
        return auth(token::toCharArray);
    }

    /**
     * Creates a customized client
     * @param params A configuration parameters builder
     */
    public static Client basic(Consumer<ConfigBuilder> params){
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
    public ClientAuth withToken(String token) {
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
    public ClientAuth withToken(Supplier<char[]> token) {
        var config = config().withToken(token);
        return new ClientAuth(config);
    }

    /**
     * Creates a customizable client using the provided configuration parameters builder.<br>
     * @param params A configuration parameters builder
     */
    public static ClientAuth auth(Consumer<ConfigBuilder> params, Supplier<char[]> token) { return basic(params).withToken(token); }


    /**
     * Creates a customizable client using the provided configuration parameters builder.<br>
     * @param params A configuration parameters builder
     */
    public static ClientAuth auth(Consumer<ConfigBuilder> params, String token) { return auth(params, token::toCharArray); }

    /**
     * Creates a default client using the provided token to use the authenticated parts of the API
     * @param token A token to use for the authenticated parts of the API
     */
    public static ClientAuth auth(Supplier<char[]> token) { return auth(c -> {}, token); }

    public sealed interface AuthResult {}
    public record AuthOk(ClientAuth client) implements AuthResult {}
    public record AuthFail(String message)  implements AuthResult {}
    public record CodeAndState(String code, String state) {}

    public interface PkceConfig {

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
    public AuthResult withPkce(Consumer<URI> uriHandler, Consumer<PkceConfig> pkce) {
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
    public static AuthResult auth(Consumer<URI> uriHandler, Consumer<PkceConfig> pkce) {
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
    public static AuthResult auth(Consumer<ConfigBuilder> config, Consumer<URI> uriHandler, Consumer<PkceConfig> pkce) {
        return basic(config).withPkce(uriHandler, pkce);
    }

    /**
     * Creates a customized client from a preferences node<br>
     * See {@link Client#store(Preferences)}
     * @param prefs A configuration preferences node<br>
     * {@code if (client instanceof ClientAuth auth) ...}
     */
    public static Client load(Preferences prefs) {
        return load(Config.load(prefs));
    }


    /**
     * Creates an authenticated customized client from a preferences node with provided token<br>
     * @param prefs A configuration preferences node
     * @param token A token to use for the authenticated parts of the API
     */
    public static ClientAuth load(Preferences prefs, String token) {
        return load(prefs).withToken(token);
    }


    ///**
    // * Retrieves an Optional containing a {@code ClientAuth} if this is such a client, otherwise empty.
    // */
    //Optional<ClientAuth> asAuth() {
    //    return this instanceof Default def && def.config() instanceof Config.Auth a ? Optional.of((ClientAuth)this) : Optional.empty();
    //}

    /**
     * Configure logging levels
     */
    public void logging(Consumer<LoggingBuilder> params) {
        var logging = config().logging();
        var builder = Config.loggingBuilder(logging);
        params.accept(builder);
    }

    private static Client load(Config config) {
        // hmm, check if auth?
        return new Client(config);
    }

    private static Client basic(Config.Basic config) {
        return new Client(config);
    }


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
