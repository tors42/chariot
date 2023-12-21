package chariot.model;

import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

public record MyRound(Tour tour, Round round, Study study) {
    public record Tour(String id, String slug, String name, String description, boolean official) {}
    public record Round(String id, String slug, String name, Optional<ZonedDateTime> startsAt, boolean ongoing, boolean finished, URI url, Duration delay) {}
    public record Study(boolean writeable) {}
}
