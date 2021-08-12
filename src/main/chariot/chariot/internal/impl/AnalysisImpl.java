package chariot.internal.impl;

import java.util.Map;

import chariot.internal.Base;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.model.Result;

public class AnalysisImpl extends Base implements Internal.Analysis {

    public AnalysisImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<chariot.model.Analysis> cloudEval(Map<String, Object> map) {
        var request = Endpoint.cloudEval.newRequest()
            .query(map)
            .build();

        return fetchOne(request);
    }

}
