package chariot.internal.impl;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import chariot.api.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.ChallengeTokens;
import chariot.model.One;

public class AdminAuthImpl extends Base implements AdminAuth {
    public AdminAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public One<ChallengeTokens> obtainChallengeTokens(Set<String> userIds, Consumer<Params> consumer) {
        return Endpoint.apiAdminChallengeTokens.newRequest(request -> request
                .body(MapBuilder.of(Params.class)
                    .add("users", userIds.stream().collect(Collectors.joining(",")))
                    .toMap(consumer)))
            .process(this);
    }
}
