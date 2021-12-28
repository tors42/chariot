package chariot.internal.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import chariot.Client.Scope;
import chariot.internal.Base;
import chariot.internal.Endpoint;
import chariot.internal.Util;
import chariot.internal.InternalClient;
import chariot.internal.PKCE;
import chariot.model.Result;
import chariot.model.TokenBulkResult;
import chariot.model.TokenResult;

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

        var parameterString = Util.urlEncode(parameters);

        var request = Endpoint.apiToken.newRequest()
            .post(parameterString)
            .build();

        var res = fetchOne(request);

        if (res.isPresent()) {
            return res.get();
        } else {
            return new TokenResult.Error("Unknown Error", "Unknonw");
        }
    }

    @Override
    public Result<TokenBulkResult> testTokens(Set<String> tokens) {

        var request = Endpoint.apiTokenBulkTest.newRequest()
            .post(tokens.stream().collect(Collectors.joining(",")))
            .build();

        return fetchOne(request);
    }
}
