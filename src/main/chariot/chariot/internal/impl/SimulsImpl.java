package chariot.internal.impl;

import chariot.api.One;
import chariot.api.Simuls;
import chariot.internal.Base;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;

public class SimulsImpl extends Base implements Simuls {
    public SimulsImpl(InternalClient client) {
        super(client);
    }

    @Override
    public One<chariot.model.Simuls> simuls() {
        return Endpoint.simuls.newRequest(request -> {})
            .process(this);
    }
}
