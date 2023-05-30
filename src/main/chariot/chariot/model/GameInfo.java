package chariot.model;

import java.util.Optional;

import chariot.model.Enums.Color;
import chariot.model.Enums.Speed;
import chariot.model.Enums.Status;

public record GameInfo(
        String fullId,
        String gameId,
        String fen,
        Color color,
        Status status,
        VariantType variant,
        TimeInfo time,
        boolean rated,
        boolean hasMoved,
        boolean isMyTurn,
        Opponent opponent,
        String source,
        TournamentInfo tournament
        ) {

    public sealed interface TimeInfo permits Enums.Speed, Time {
        default Speed speed() { return this instanceof Time sl ? sl.speed() : (Speed) this; }
        default Optional<Integer> secondsLeftOpt() { return this instanceof Time sl
            ? Optional.of(sl.secondsLeft()) : Optional.empty();
        }
    }
    public record Time(Speed speed, int secondsLeft) implements TimeInfo {}

    public sealed interface TournamentInfo permits ArenaId, SwissId, None {}
    public record ArenaId(String id) implements TournamentInfo {}
    public record SwissId(String id) implements TournamentInfo {}
    public record None()             implements TournamentInfo {}

    public sealed interface Opponent permits AI, Anonymous, Account, AccountDiff {
        default String id() {
            if (this instanceof Account acc) return acc.id();
            if (this instanceof AccountDiff acc) return acc.account().id();
            return "";
        }
        default String name() {
            if (this instanceof AI ai) return ai.name();
            if (this instanceof Anonymous) return "Anonymous.";
            if (this instanceof Account acc) return acc.username();
            if (this instanceof AccountDiff acc) return acc.account().username();
            return "";
        }
    }
    public record AI(int level, String name)                      implements Opponent {}
    public record Anonymous()                                     implements Opponent {}
    public record Account(String id, String username, int rating) implements Opponent {}
    public record AccountDiff(Account account, int ratingDiff)    implements Opponent {}

}
