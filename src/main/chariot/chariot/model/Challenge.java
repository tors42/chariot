package chariot.model;

import java.util.List;

public sealed interface Challenge permits
    ChallengeInfo,
    Challenge.ChallengeWithRules,
    Challenge.DeclinedChallenge,
    Challenge.RematchChallenge {

        default String id() {
            if (this instanceof ChallengeInfo info)           return info.id();
            if (this instanceof ChallengeWithRules withRules) return withRules.challenge().id();
            if (this instanceof DeclinedChallenge declined)   return declined.challenge().id();
            if (this instanceof RematchChallenge rematch)     return rematch.challenge().id();
            return "";
        }

        record ChallengeWithRules(List<String> rules, Challenge challenge) implements Challenge {}
        record RematchChallenge(String rematchOf, Challenge challenge) implements Challenge {}
        record DeclinedChallenge(String key, String reason, Challenge challenge) implements Challenge {}
}
