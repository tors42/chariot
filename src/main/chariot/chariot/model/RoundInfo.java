package chariot.model;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;

import chariot.internal.Util;

public record RoundInfo(Round round, Tour tour, Study study, List<Game> games, Opt<Broadcast.Group> group)  {

    public String id() { return round().id(); }

    public record Round(String id, String name, String slug, ZonedDateTime createdAt, boolean ongoing, boolean finished, Opt<ZonedDateTime> startsAt, URI url) {}
    public record Tour(String id, String name, String slug, String description, ZonedDateTime createdAt, int tier, Opt<URI> image) {}
    public record Study(boolean writeable) {}

    public sealed interface Game {
        String id();
        String name();

        default Opt<String> fenOpt() { return this instanceof WithInfo gi ? Opt.of(gi.fen()) : Opt.empty(); }
        default List<Player> players() { return this instanceof WithInfo gi ? gi.players() : List.of(); }
        default Opt<String> lastMoveOpt() { return this instanceof WithInfo gi ? Opt.of(gi.lastMove()) : Opt.empty(); }
        default Opt<Integer> thinkTimeOpt() { return this instanceof WithInfo gi ? Opt.of(gi.thinkTime()) : Opt.empty(); }
        default Opt<String> statusOpt() { return this instanceof WithInfo gi ? Opt.of(gi.status()) : Opt.empty(); }
    }
    public record ChapterIdAndName(String id, String name) implements Game {}
    public record WithInfo(String id, String name, String fen, List<Player> players, String lastMove, int thinkTime, String status) implements Game {}
    public record Player(String name, Opt<String> title, Opt<Integer> rating, int clock, Opt<String> fed) {}

    public List<WithInfo> gamesWithInfo() {
        return Util.filterCast(games, WithInfo.class).toList();
    }
}
