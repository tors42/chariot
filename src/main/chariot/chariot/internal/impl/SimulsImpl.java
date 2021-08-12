package chariot.internal.impl;

import chariot.api.Simuls;
import chariot.internal.Base;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.model.Result;

public class SimulsImpl extends Base implements Simuls {

    public SimulsImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<chariot.model.Simuls> simuls() {
        var request = Endpoint.simuls.newRequest()
            .build();
        return fetchOne(request);
    }

}
