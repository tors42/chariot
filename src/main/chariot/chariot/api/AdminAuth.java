package chariot.api;

import java.util.Set;

import chariot.model.ChallengeTokens;
import chariot.model.Result;

public interface AdminAuth {

    /**
     * For administrators only. You are not allowed to use this endpoint.
     * Create and obtain challenge:write tokens for multiple users.
     * If a similar token already exists for a user, it is reused. This endpoint is idempotent.
     */
    Result<ChallengeTokens> obtainChallengeTokens(Description description, Set<String> userIds);

    default Result<ChallengeTokens> obtainChallengeTokens(String... userIds) {
        return obtainChallengeTokens(Description.of("Created by Admin"), Set.of(userIds));
    }

    default Result<ChallengeTokens> obtainChallengeTokens(Description description, String... userIds) {
        return obtainChallengeTokens(description, Set.of(userIds));
    }

    record Description(String description) {
        public static Description of(String description) {
            return new Description(description);
        }
    }
}
