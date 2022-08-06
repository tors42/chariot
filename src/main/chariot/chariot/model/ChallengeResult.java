package chariot.model;

import chariot.internal.Util;

public sealed interface ChallengeResult permits
    ChallengeAI,
    ChallengeOpenEnded,
    ChallengeResult.ChallengeInfo,
    ChallengeResult.OpponentDecision {

    public record ChallengeInfo(Challenge challenge) implements ChallengeResult {}
    public record OpponentDecision(String done) implements ChallengeResult {}

    public sealed interface TimeControl permits TimeControl.Clock, TimeControl.Days {
        record Clock(String type, String show, int limit, int increment) implements TimeControl {}
        record Days(String type, int daysPerTurn) implements TimeControl {}
    }

    public record Player(
            String id,
            String name,
            String title,
            boolean online,
            boolean provisional,
            int rating,
            boolean patron,
            Integer lag) {

        public Player {
            title = Util.orEmpty(title);
        }
    }

    public record Perf(String icon, String name) {}
}
