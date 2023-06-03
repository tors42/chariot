package chariot.model;

import java.util.List;

public sealed interface Challenge permits
    ChallengeInfo,
    Challenge.ChallengeWithRules,
    Challenge.DeclinedChallenge,
    Challenge.RematchChallenge {

        record ChallengeWithRules(List<String> rules, Challenge challenge) implements Challenge {}
        record RematchChallenge(String rematchOf, Challenge challenge) implements Challenge {}
        record DeclinedChallenge(String key, String reason, Challenge challenge) implements Challenge {}
}
