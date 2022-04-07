package chariot.model;

import java.util.Optional;

public record UserStatus(
        String id,
        String name,
        Optional<String> title,
        Optional<String> playingId,
        boolean online,
        boolean playing,
        boolean streaming,
        boolean patron) implements Model {
}
