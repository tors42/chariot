package chariot.model;

import java.util.Optional;

public record UStatus(UserCommon common, boolean online, boolean playing) implements UserStatus {
}
