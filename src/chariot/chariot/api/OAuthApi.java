package chariot.api;

import module java.base;
import module chariot;

public interface OAuthApi {

    URI requestAuthorizationCodeURI(Consumer<Params> params);

    One<AccessToken> obtainAccessToken(Consumer<AccessTokenParams> params);

    One<TokenStatus> testTokens(Collection<String> tokens);
    default One<TokenStatus> testTokens(String... tokens) {
        return testTokens(Arrays.asList(tokens));
    }

    Many<Client.Scope> scopes(String token);

    URI personalAccessTokenForm(String description, Client.Scope... scopes);

    interface Params {
        /// @param client_id Arbitrary identifier that uniquely identifies your application.
        Params client_id(String client_id);

        /// @param redirect_uri The URI the user should be redirected to with the authorization result.
        Params redirect_uri(URI redirect_uri);
        /// @param redirect_uri The URI the user should be redirected to with the authorization result.
        default Params redirect_uri(String redirect_uri) {
            return redirect_uri(URI.create(redirect_uri));
        }

        /// @param code_challenge Compute `BASE64URL(SHA256(code_verifier))`.
        Params code_challenge(String code_challenge);

        /// @param scopes Requested OAuth scopes, if any.
        Params scope(String... scopes);
        /// @param scopes Requested OAuth scopes, if any.
        default Params scope(Client.Scope... scopes) {
            return scope(Arrays.stream(scopes)
                    .map(Client.Scope::asString)
                    .toArray(String[]::new));
        }

        default Params scope(Set<Client.Scope> scopes) {
            return scope(scopes.stream()
                    .map(Client.Scope::asString)
                    .toArray(String[]::new));
        }


        /// @param username Hint that you want the user to log in with a specific Lichess username.
        Params username(String username);

        /// @param state Arbitrary state that will be returned verbatim with the authorization result.
        Params state(String state);

        /// @param code_challenge_method Must be `S256`. Default `S256`.
        Params code_challenge_method(String code_challenge_method);
        /// @param response_type Must be `code`. Default `code`.
        Params response_type(String response_type);
    }

    interface AccessTokenParams {
        /// @param code The authorization code that was sent in the code parameter to your `redirect_uri`.
        AccessTokenParams code(String code);
        /// @param client_id Must match the `client_id` used to request the authorization code.
        AccessTokenParams client_id(String client_id);
        /// A `code_challenge` was used to request the authorization code.
        /// @param code_verifier This must be the `code_verifier` it was derived from.
        AccessTokenParams code_verifier(String code_verifier);
        /// @param redirect_uri Must match the `redirect_uri` used to request the authorization code.
        AccessTokenParams redirect_uri(URI redirect_uri);
        default AccessTokenParams redirect_uri(String redirect_uri) {
            return redirect_uri(URI.create(redirect_uri));
        }

        /// @param grant_type Default `authorization_code`
        AccessTokenParams grant_type(String grant_type);
    }
}
