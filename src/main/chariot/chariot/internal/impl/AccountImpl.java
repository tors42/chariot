package chariot.internal.impl;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import chariot.Client.Scope;
import chariot.api.*;
import chariot.internal.*;
import chariot.model.*;

public class AccountImpl extends Base implements chariot.api.Account {

    AccountImpl(InternalClient client) {
        super(client);
    }

    @Override
    public UriAndToken oauthPKCE(Scope... scopes) {
        try {
            var uriAndToken = PKCE.initiateAuthorizationFlow(Set.of(scopes), client.config().servers().api().get(), this::token);
            return uriAndToken;
        } catch (Exception e) {
            // Hmm... Prolly fail more gracefully
            throw new RuntimeException(e);
        }
    }

    @Override
    public UriAndTokenExchange oauthPKCEwithCustomRedirect(URI customRedirectUri, Scope... scopes) {
        try {
            var uriAndToken = PKCE.initiateAuthorizationFlowCustom(Set.of(scopes), client.config().servers().api().get(), this::token, customRedirectUri);
            return uriAndToken;
        } catch (Exception e) {
            // Hmm... Prolly fail more gracefully
            throw new RuntimeException(e);
        }
     }

    @Override
    public URL personalAccessTokenForm(String description, Scope... scopes) {
        // https://lichess.org/account/oauth/token/create?scopes[]=challenge:write&scopes[]=puzzle:read&description=Prefilled+token+example
        var scopesString = Set.of(scopes).stream()
            .map(s -> "scopes[]=" + s.asString())
            .collect(Collectors.joining("&"));

        var descriptionString = "description=" + URLEncoder.encode(description, StandardCharsets.UTF_8);

        var server = client.config().servers().api().get();
        var endpoint = Endpoint.accountOAuthToken.endpoint() + "/create" + "?" ;
        var params = String.join("&", scopesString, descriptionString);

        var uri = URI.create(server + endpoint + params);
        try {
            var url = uri.toURL();
            return url;
        } catch (MalformedURLException mue) {
            throw new RuntimeException(mue);
        }
    }

    TokenResult token(Map<String, String> parameters) {
        return Endpoint.apiToken.newRequest(request -> request
                .body(parameters))
            .process(this) instanceof Entry<TokenResult> tr ?
            tr.entry() : new TokenResult.Error("Unknown Error", "Unknown");
    }

    @Override
    public Set<Scope> scopes(Supplier<char[]> token) {
        var scopes = client.fetchScopes(Endpoint.accountProfile.endpoint(), token);
        return scopes;
    }

    @Override
    public One<TokenBulkResult> testTokens(Set<String> tokens) {
        return Endpoint.apiTokenBulkTest.newRequest(request -> request
                .body(tokens.stream().collect(Collectors.joining(","))))
            .process(this);
    }
}
