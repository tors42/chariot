package chariot.model;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;

public record RoundInfo(Broadcast.RoundByUser round, Tour tour, Study study, List<Game> games, Opt<Broadcast.Group> group)  {

    public String id() { return round().id(); }

    // Note, Tour teamTable is not present available in the endpoint this model is used,
    // so sadly can't reuse Broadcast.Tour - because a missing teamTable field here does not mean `false` - it is just not known...
    public record Tour(String id, String name, String slug, Opt<Broadcast.Info> info, ZonedDateTime createdAt, URI url, int tier, List<ZonedDateTime> dates, Opt<URI> image) {}

    public record Features(boolean chat, boolean computer, boolean explorer) {}
    public record Study(boolean writeable, Features features) {}

    public record Game(String id, String name, String fen, List<Player> players, String lastMove, String check, Opt<Integer> thinkTime, String status) {}
    public record Player(String name, Opt<String> title, Opt<Integer> rating, Opt<Integer> fideId, Opt<String> team, Opt<String> fed, int clock) {}
}
