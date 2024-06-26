package chariot.model;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;

public record RoundInfo(Round round, Tour tour, Study study, List<Game> games, Opt<Broadcast.Group> group)  {

    public String id() { return round().id(); }

    public record Round(String id, String name, String slug, ZonedDateTime createdAt, boolean ongoing, boolean finished, Opt<ZonedDateTime> startsAt, URI url) {}
    public record Tour(String id, String name, String slug, String description, ZonedDateTime createdAt, int tier, Opt<URI> image) {}
    public record Study(boolean writeable) {}

    public record Game(String id, String name, String fen, List<Player> players, String lastMove, int thinkTime, String status) {}
    public record Player(String name, Opt<String> title, Opt<Integer> rating, int clock, Opt<String> fed) {}
}
