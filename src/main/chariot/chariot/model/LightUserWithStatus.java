package chariot.model;

import static chariot.internal.Util.orEmpty;

public record LightUserWithStatus (String id, String name, String title, boolean patron, boolean online) {
    public LightUserWithStatus {
        title = orEmpty(title);
    }
}
