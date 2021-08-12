package chariot.internal;

import chariot.Client;

public final class BasicClient extends DefaultClient implements Client.Basic {
    public BasicClient(Config.Basic basic) {
        super(basic);
    }
}
