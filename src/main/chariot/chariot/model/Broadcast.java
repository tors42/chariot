package chariot.model;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public record Broadcast(Tour tour, List<Round> rounds)  {
    public String id() { return tour().id(); }

    public record Tour(String id, String name, String slug, String description, ZonedDateTime createdAt, int tier, String markup, URI url, Opt<URI> image, boolean teamTable, boolean leaderboard) {}
    public record Round(String id, String name, String slug, ZonedDateTime createdAt, boolean ongoing, boolean finished, Optional<ZonedDateTime> startsAt, RoundTour tour, URI url) {}
    public record RoundTour(String id, String name, String slug, String description, ZonedDateTime createdAt, int tier) {}
}
