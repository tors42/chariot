package chariot.model;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public record Broadcast(Tour tour, List<Round> rounds)  {
    public record Tour(String id, String name, String slug, String description, ZonedDateTime createdAt, boolean official, String markup, URI url) {}
    public record Round(String id, String name, String slug, ZonedDateTime createdAt, boolean ongoing, boolean finished, Optional<ZonedDateTime> startsAt, RoundTour tour, URI url) {}
    public record RoundTour(String id, String name, String slug, String description, ZonedDateTime createdAt, boolean official) {}
}
