package chariot.internal.impl;

import module java.base;
import module chariot;
import chariot.Client.Scope;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.internal.RequestHandler;
import chariot.internal.Util;
import chariot.internal.Util.MapBuilder;

class OAuthHandler implements OAuthAuthApi {
    private final InternalClient client;
    private final RequestHandler requestHandler;

    public OAuthHandler(InternalClient client, RequestHandler requestHandler) {
        this.client = client;
        this.requestHandler = requestHandler;
    }

    @Override
    public URI requestAuthorizationCodeURI(Consumer<Params> params) {
        var queryMap = MapBuilder.of(Params.class)
            .add("response_type", "code")
            .add("code_challenge_method", "S256")
            .addCustomHandler("scope", (args, map) -> {
                if (args[0] instanceof String[] scopeArr)
                    map.put("scope", Arrays.stream(scopeArr).collect(Collectors.joining(" ")));
            })
            .toMap(params);

        String queryParams = Util.urlEncode(queryMap);
        return client.config().servers().api().resolve(Endpoint.oauth.endpoint() + "?" + queryParams);
    }

    @Override
    public One<AccessToken> obtainAccessToken(Consumer<AccessTokenParams> params) {
        var bodyMap = MapBuilder.of(AccessTokenParams.class)
            .add("grant_type", "authorization_code")
            .toMap(params);

        return Endpoint.oauthToken.newRequest(request -> request
                .body(bodyMap)
                ).process(requestHandler);
    }

    @Override
    public One<TokenStatus> testTokens(Collection<String> tokens) {
        return Endpoint.oauthTestTokens.newRequest(request -> request
                .body(String.join(",", tokens))
                ).process(requestHandler);
    }

    @Override
    public Ack revokeToken() {
        return Endpoint.oauthRevoke.newRequest(_ -> {})
            .process(requestHandler);
    }

    @Override
    public URI personalAccessTokenForm(String description, Scope... scopes) {
        // https://lichess.org/account/oauth/token/create?scopes[]=challenge:write&scopes[]=puzzle:read&description=Prefilled+token+example
        String scopesString = Set.of(scopes).stream()
            .map(s -> "scopes[]=" + s.asString())
            .collect(Collectors.joining("&"));
        String descriptionString = "description=" + URLEncoder.encode(description, StandardCharsets.UTF_8);
        return client.config().servers().api().resolve(
                Endpoint.accountOAuthToken.endpoint() + "/create?" +
                String.join("&", scopesString, descriptionString));
    }

    @Override
    public Many<Scope> scopes(String token) {
        return client.fetchScopes(Endpoint.accountProfile.endpoint(), token::toCharArray);
    }

    @Override
    public Many<Client.Scope> scopes() {
        return client.fetchScopes(Endpoint.accountProfile.endpoint());
    }

}
