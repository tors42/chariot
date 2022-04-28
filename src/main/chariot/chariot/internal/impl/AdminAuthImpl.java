package chariot.internal.impl;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import chariot.api.AdminAuth;
import chariot.internal.Base;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.model.ChallengeTokens;
import chariot.model.Result;

public class AdminAuthImpl extends Base implements AdminAuth {

    public AdminAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<ChallengeTokens> obtainChallengeTokens(Description description, Set<String> userIds) {
        var ids = userIds.stream()
            .collect(Collectors.joining(","));

        var request = Endpoint.apiAdminChallengeTokens.newRequest()
            .post(Map.of("users", ids, "description", description.description()))
            .build();
        return fetchOne(request);
    }

}
