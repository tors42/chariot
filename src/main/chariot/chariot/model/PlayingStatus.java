package chariot.model;

import java.util.Optional;

public record PlayingStatus(UStatus status, String gameId) implements UserStatus {
}
