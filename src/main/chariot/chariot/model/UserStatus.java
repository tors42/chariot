package chariot.model;

import static chariot.internal.Util.orEmpty;

public record UserStatus(
        String id,
        String name,
        String title,
        boolean online,
        boolean playing,
        boolean streaming,
        boolean patron) implements Model {

    public UserStatus {
        title = orEmpty(title);
    }

}
