package chariot.model;

import chariot.internal.Util;

public record Trophy(String type, String name, long date) implements Model {

    public java.time.ZonedDateTime dateAt() {
        return Util.fromLong(date());
    }

}
