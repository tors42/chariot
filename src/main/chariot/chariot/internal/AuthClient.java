package chariot.internal;

import chariot.ClientAuth;

public final class AuthClient extends DefaultClient implements ClientAuth {
    public AuthClient(Config.Auth config) {
        super(config);
    }
}
