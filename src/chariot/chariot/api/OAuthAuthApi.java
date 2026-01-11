package chariot.api;

import module chariot;

public interface OAuthAuthApi extends OAuthApi {
    Ack revokeToken();
    Many<Client.Scope> scopes();
}
