package chariot.model;

import java.util.List;

public record PendingChallenges(List<ChallengeResult.ChallengeInfo.Challenge> in, List<ChallengeResult.ChallengeInfo.Challenge> out) implements Model {}
