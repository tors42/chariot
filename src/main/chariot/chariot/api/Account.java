package chariot.api;

import java.net.*;
import java.util.Set;
import java.util.function.Supplier;

import chariot.Client;
import chariot.Client.Scope;
import chariot.model.TokenBulkResult;

/**
 * Helpers for obtaining authorization tokens
 * <ul>
 * <li> <a href="https://oauth.net/2/pkce/">PKCE Authorization Code flow</a> - {@link #oauthPKCE oauthPKCE(scopes...)}
 * <li> <a href="https://lichess.org/account/oauth/token">Personal Access Tokens</a> - {@link #personalAccessTokenForm personalAccessTokenForm(scopes...)}
 * </ul>
 */
public interface Account {

    /**
     * Use OAuth 2.0 with Proof Key Code Exchange to make it possible for a user
     * to authorize the application to act on the behalf of the user for
     * specified scopes.
     *
     * <p>This authorization flow requires interaction between the user and
     * Lichess, and that takes place on a so called "front channel" - outside of
     * the application.
     *
     * <p>An example usage, where an application used for creating tournaments
     * would request a token for {@link Client.Scope#tournament_write}, could
     * look like this,
     * {@snippet :
     *    Client client = Client.basic();
     *    var urlAndToken = client.account().oauthPKCE(Scope.tournament_write);
     *    ...
     * }
     * <p>The response contains an URL which can be used to ask the user to grant
     * access. Web applications typically just redirects from the application
     * page to the Lichess page - but for non-browser applications it is a bit
     * trickier. It might be possible to use a WebView component, but the user
     * would then need to enter their password into the application. A better
     * option would be be to launch the user's system browser instead - then the
     * user don't need to enter their password in other places than where they
     * usually do.
     *
     * <p>The following code uses the {@code java.desktop.Desktop#browse}
     * support to achieve this. It has the possibiltiy to launch the user's
     * browser and navigate to the Lichess page. If the user is already signed
     * in, they will immediately be able to click the grant or deny options.
     * Otherwise they need to login and will then be forwarded to the grant/deny
     * page - all taking place without the application needing to know that the
     * user authenticates with Lichess.
     * {@snippet :
     *    URI lichessUrl = urlAndToken.url();
     *
     *    // Launch the user's system browser (i.e Firefox or similar),
     *    // and direct them to the Lichess
     *    Desktop.getDesktop().browse(lichessUrl);
     *    ...
     * }
     *
     * <p>After the user has finished the grant process, Lichess will redirect
     * the user's browser to a URL which was provided as a parameter in the
     * {@code lichessUrl} query parameters. The redirect URL has been set to a
     * localhost address which Chariot starts listening on when starting the
     * PKCE flow. Chariot can continue to verify if the permission was indeed
     * granted and that a access token now can be retrieved from Lichess. After
     * this, the application can now retrieve the token from the {@code
     * urlAndToken} and load a ClientAuth client with the token which permits
     * creating tournaments.
     * {@snippet :
     *    // Blocks until user has finished grant process
     *    Supplier<char[]> token = urlAndToken.token().get();
     *
     *    ClientAuth authClient = Client.auth(token);
     *    var result = authClient.tournaments().createArena( params -> ... );
     *
     *    // When we are done, we can delete the token (making it no longer valid)
     *    authClient.account().revokeToken();
     * }
     *
     * @param scopes The scopes that the authorization token must be valid for
     * @return The {@code UriAndToken} contains the URL to use for front channel
     * communication, and a {@code Supplier} of the authorization token - which
     * will supply a value after the front channel communication has succeeded.
     */
    UriAndToken oauthPKCE(Scope... scopes);

    UriAndTokenExchange oauthPKCEwithCustomRedirect(URI customRedirectUri, Scope... scopes);

    /**
     * Helper method for creating <a href="https://lichess.org/account/oauth/token">Personal Access Tokens</a>
     * <p>Note, a user must create the token manually.
     * <p>See also {@link #oauthPKCE oauthPKCE(scopes)}
     * {@snippet :
     *     Client client = Client.basic();
     *     var url = client.account().personalAccessTokenForm("Token for reading preferences", Scope.preferences_read);
     *
     *     System.out.println("Please create a token at Lichess,\nusing this pre-filled form: " + url);
     *     System.out.println("Copy and paste the token when done");
     *
     *     // Wait for the token
     *     String token = System.console().readLine();
     *
     *     ClientAuth clientAuth = Client.auth(token);
     *     var email = clientAuth.account().emailAddress();
     * }
     * @param description A description for the intended use of the token, so the user can easier recognize the token when browsing their security information.
     * @param scopes The pre-selected scopes for the token
     * @return A URL where user can create a token, in order to then copy the token and use as input to an application.<br>
     * Example: https://lichess.org/account/oauth/token/create?scopes[]=challenge:write{@literal &}scopes[]=puzzle:read{@literal &}description=Prefilled+token+example
     */
    URL personalAccessTokenForm(String description, Scope... scopes);

    record UriAndToken(URI url, Supplier<Supplier<char[]>> token) {}

    interface UriAndTokenExchange {
        URI url();
        /**
         * {@see #oauthPKCEwithCustomRedirect}
         *
         * @param code the authorization code which was obtained in the redirect from Lichess
         * @param state the state which was obtained in the redirect from Lichess
         */
        Supplier<Supplier<char[]>> token(String code, String state);
    }

    One<TokenBulkResult> testTokens(Set<String> tokens);

    default One<TokenBulkResult> testTokens(String... tokens) {
        return testTokens(Set.of(tokens));
    }

    /**
     * Read which scopes are available with a token
     * @param token
     */
    Set<Scope> scopes(Supplier<char[]> token);

    /**
     * See {@link chariot.api.Account#scopes(Supplier)}
     */
     default Set<Scope> scopes(String token) {
        return scopes(() -> token.toCharArray());
    }

}
