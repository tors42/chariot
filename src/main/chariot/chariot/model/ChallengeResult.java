package chariot.model;

import chariot.model.Enums.*;
import chariot.internal.Util;
import static chariot.internal.Util.orEmpty;

import java.util.Optional;

public sealed interface ChallengeResult permits
    ChallengeResult.ChallengeInfo, ChallengeResult.ChallengeAI, ChallengeResult.ChallengeOpenEnded, ChallengeResult.OpponentDecision {

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
            Color finalColor,
            Variant variant,
            Player challenger,
            Player destUser,
            Perf perf,
            Optional<String> declineReason,
            Optional<String> rematchOf) {

            public record Variant (GameVariant key, String name) {}

            public Challenge {
                status = orEmpty(status);
            }
    }

    public record ChallengeAI(
            String id,
            String speed,
            String perf,
            String source,
            boolean rated,
            int turns,
            int startedAtTurn,
            Long createdTime,
            Status status,
            Variant variant,
            Color player,
            Optional<String> initialFen
            ) implements ChallengeResult {

            public ChallengeAI {
                source = orEmpty(source);
            }

            public java.time.ZonedDateTime createdAt() {
                return Util.fromLong(createdTime());
            }

            public record Variant(GameVariant key, String name, String shortname) {}
            public record Status(int id, String name) {}
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
