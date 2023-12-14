package chariot.model;

import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;

public record BCRound(Tour tour, Round round, Study study) {
    public record Tour(String id, String slug, String name, String description, boolean official) {}
    public record Round(String id, String slug, String name, ZonedDateTime startsAt, boolean finished, URI url, Duration delay) {}
    public record Study(boolean writable) {}
}
