package chariot.model;
import java.util.Optional;

import chariot.internal.Util;
import chariot.model.Enums.*;

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
        source = Util.orEmpty(source);
    }

    public java.time.ZonedDateTime createdAt() {
        return Util.fromLong(createdTime());
    }

    public record Variant(GameVariant key, String name, String shortname) {}
    public record Status(int id, String name) {}
}
