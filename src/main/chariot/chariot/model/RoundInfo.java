package chariot.model;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public record RoundInfo(Round round, Tour tour, Study study, List<Game> games)  {
    public record Round(String id, String name, String slug, boolean ongoing, boolean finished, Optional<ZonedDateTime> startsAt, URI url) {}
    public record Tour(String id, String name, String slug, String description, boolean official) {}
    public record Study(boolean writeable) {}
    public record Game(String id, String name, boolean ongoing, String res, URI url) {}
}
