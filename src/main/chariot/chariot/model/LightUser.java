package chariot.model;

import static chariot.internal.Util.orEmpty;

public record LightUser (String id, String name, String title, boolean patron) {

    public LightUser {
        title = orEmpty(title);
    }

}
