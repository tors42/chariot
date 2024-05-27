package chariot.model;

import java.util.List;

public sealed interface Challenge permits
    ChallengeInfo,
    Challenge.ChallengeWithRules,
    Challenge.DeclinedChallenge,
    Challenge.RematchChallenge {

        default String id() {
            return switch(this) {
                case ChallengeInfo info -> info.id();
                case ChallengeWithRules withRules -> withRules.challenge().id();
                case DeclinedChallenge declined -> declined.challenge().id();
                case RematchChallenge rematch -> rematch.challenge().id();
            };
        }

        default List<String> rules() { return List.of(); }

        record ChallengeWithRules(List<String> rules, Challenge challenge) implements Challenge {}
        record RematchChallenge(String rematchOf, Challenge challenge) implements Challenge {}
        record DeclinedChallenge(String key, String reason, Challenge challenge) implements Challenge {}
}
