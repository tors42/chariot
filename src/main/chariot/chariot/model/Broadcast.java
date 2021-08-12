package chariot.model;

import java.util.List;

import chariot.internal.Util;

public record Broadcast(Tournament tour, List<Round> rounds) implements Model {

    public record Tournament(String id, String name, String slug, String url, String description, String markup) {}

    public record Round(String id, String name, String slug, String url, boolean ongoing, boolean finished, Long startsTime) implements Model {
        public java.time.ZonedDateTime startsAt() {
            return Util.fromLong(startsTime());
        }
    }

}
