package chariot.internal.impl;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import chariot.Client.Scope;
import chariot.internal.*;
import chariot.model.*;

public class TokenHandler {

    private final InternalClient client;
    private final RequestHandler requestHandler;

    public TokenHandler(InternalClient client, RequestHandler requestHandler) {
        this.client = client;
        this.requestHandler = requestHandler;
    }

    public URI personalAccessTokenForm(String description, Scope... scopes) {
        // https://lichess.org/account/oauth/token/create?scopes[]=challenge:write&scopes[]=puzzle:read&description=Prefilled+token+example
        var scopesString = Set.of(scopes).stream()
            .map(s -> "scopes[]=" + s.asString())
            .collect(Collectors.joining("&"));

        var descriptionString = "description=" + URLEncoder.encode(description, StandardCharsets.UTF_8);

        var server = client.config().servers().api().toString();
        var endpoint = Endpoint.accountOAuthToken.endpoint() + "/create" + "?" ;
        var params = String.join("&", scopesString, descriptionString);

        var uri = URI.create(server + endpoint + params);
        return uri;
    }

    public TokenResult token(Map<String, String> parameters) {
        return Endpoint.apiToken.newRequest(request -> request
                .body(parameters))
            .process(requestHandler) instanceof Entry<TokenResult> tr ?
            tr.entry() : new TokenResult.Error("Unknown Error", "Unknown");
    }

    public Many<Scope> scopes(Supplier<char[]> token) {
        return client.fetchScopes(Endpoint.accountProfile.endpoint(), token);
    }

    public Many<Scope> scopes() {
        return client.fetchScopes(Endpoint.accountProfile.endpoint());
    }

    public One<TokenBulkResult> testTokens(Set<String> tokens) {
        return Endpoint.apiTokenBulkTest.newRequest(request -> request
                .body(tokens.stream().collect(Collectors.joining(","))))
            .process(requestHandler);
    }

    public Ack revokeToken() {
        return Endpoint.apiTokenRevoke.newRequest(request -> {})
            .process(requestHandler);
    }


    @Deprecated
    public interface UriAndTokenExchange {
        URI url();
        /**
         * {@see #oauthPKCEwithCustomRedirect}
         *
         * @param code the authorization code which was obtained in the redirect from Lichess
         * @param state the state which was obtained in the redirect from Lichess
         */
        Supplier<Supplier<char[]>> token(String code, String state);
    }


    public UriAndTokenExchange oauthPKCEwithCustomRedirect(URI customRedirectUri, Scope... scopes) {
        try {
            var uriAndToken = PKCE.initiateAuthorizationFlowCustom(Set.of(scopes), client.config().servers().api().toString(), this::token, customRedirectUri);
            return uriAndToken;
        } catch (Exception e) {
            // Hmm... Prolly fail more gracefully
            throw new RuntimeException(e);
        }
     }

    @Deprecated
    public record UriAndToken(URI url, Supplier<Supplier<char[]>> token) {}


    public UriAndToken oauthPKCE(Scope... scopes) {
        try {
            String lichessUri = client.config().servers().api().toString();
            String successPage = PKCE.successPage(lichessUri);
            var uriAndToken = PKCE.initiateAuthorizationFlow(Set.of(scopes), lichessUri, this::token, Duration.ofMinutes(2), successPage);
            return uriAndToken;
        } catch (Exception e) {
            // Hmm... Prolly fail more gracefully
            throw new RuntimeException(e);
        }
    }
}
