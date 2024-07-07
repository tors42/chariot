package chariot.model;

import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;

public record MyRound(Tour tour, Round round, Study study) {

    public String id() { return round().id(); }

    public record Tour(String id, String slug, String name, Broadcast.Info info, ZonedDateTime createdAt, int tier, Opt<URI> image) {}
    public record Round(String id, String slug, String name, ZonedDateTime createdAt, Opt<ZonedDateTime> startsAt, boolean ongoing, boolean finished, URI url, Duration delay) {}
    public record Study(boolean writeable) {}
}
