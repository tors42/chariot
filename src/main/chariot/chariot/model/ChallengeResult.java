package chariot.model;

import chariot.model.Enums.*;
import chariot.internal.Util;
import static chariot.internal.Util.orEmpty;

public sealed interface ChallengeResult extends Model {

    public record ChallengeInfo(Challenge challenge) implements ChallengeResult {}

    public record Challenge(
            String id,
            String speed,
            String url,
            String status,
            boolean rated,
            Direction direction,
            TimeControl timeControl,
            ColorPref color,
            Variant variant,
            Player challenger,
            Player destUser,
            Perf perf,
            String declineReason) {

            public Challenge {
                declineReason = orEmpty(declineReason);
                status = orEmpty(status);
            }
    }

    public record ChallengeAI(
            String id,
            String moves,
            String speed,
            String perf,
            String source,
            String status,
            boolean rated,
            Long createdTime,
            Long lastMoveTime,
            VariantName variant,
            Players players,
            Opening opening,
            Clock clock
            ) implements ChallengeResult {

            public ChallengeAI {
                moves = orEmpty(moves);
                source = orEmpty(source);
                status = orEmpty(status);
            }

            public java.time.ZonedDateTime createdAt() {
                return Util.fromLong(createdTime());
            }

            public java.time.ZonedDateTime lastMoveAt() {
                return Util.fromLong(lastMoveTime());
            }

            public record Players(Player white, Player black) {
                public record Player(LightUser user, int rating, int ratingDiff) {}
            }

            public record Clock(int initial, int increment, int totalTime) {}
    }

    public record ChallengeOpenEnded(
            Challenge challenge,
            String urlWhite,
            String urlBlack,
            int socketVersion) implements ChallengeResult {

        public record Challenge(
                String id,
                String url,
                String speed,
                String status,
                boolean rated,
                ColorPref color,
                TimeControl timeControl,
                Variant variant,
                Player challenger,
                Player destUser,
                Perf perf) {

            public Challenge {
                status = orEmpty(status);
            }
        }
    }

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
            title = orEmpty(title);
        }
    }

    public record Perf(String icon, String name) {}
}
