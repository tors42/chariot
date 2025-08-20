package chariot.api;

import java.util.Set;
import java.util.function.Consumer;

import chariot.model.ChallengeTokens;
import chariot.model.One;

public interface AdminApiAuth {
    /**
     * For administrators only. You are not allowed to use this endpoint.
     * Create and obtain challenge:write tokens for multiple users.
     * If a similar token already exists for a user, it is reused. This endpoint is idempotent.
     */
    One<ChallengeTokens> obtainChallengeTokens(Set<String> userIds, Consumer<Params> params);

    default One<ChallengeTokens> obtainChallengeTokens(Set<String> userIds) {
        return obtainChallengeTokens(userIds, params -> params.description("Created by Admin"));
    }
    default One<ChallengeTokens> obtainChallengeTokens(String... userIds) {
        return obtainChallengeTokens(Set.of(userIds));
    }
    default One<ChallengeTokens> obtainChallengeTokens(Consumer<Params> params, String... userIds) {
        return obtainChallengeTokens(Set.of(userIds), params);
    }
    interface Params {
        Params description(String description);
    }

}
