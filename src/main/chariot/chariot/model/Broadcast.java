package chariot.model;

import java.time.ZonedDateTime;
import java.util.List;

public record Broadcast(Tournament tour, List<Round> rounds)  {
    public record Tournament(String id, String name, String slug, String url, String description, String markup) {}
    public record Round(String id, String name, String slug, String url, boolean ongoing, boolean finished, ZonedDateTime startsAt) {}
}
