package chariot.model;

import java.util.List;
import java.util.function.Consumer;

import chariot.model.Enums.TournamentState;
import chariot.internal.Util;

// Lighter than Arena (Arena holds duels and standings etc)
public sealed interface Tournament {

    String id();
    String createdBy();
    String system();
    Integer minutes();
    Clock clock();
    boolean rated();
    String fullName();
    Integer nbPlayers();
    Variant variant();
    Long startsAt();
    Long finishesAt();
    TournamentState status();
    Perf perf();

    default java.time.ZonedDateTime startsTime() {
        return Util.fromLong(startsAt());
    }

    default java.time.ZonedDateTime finishesTime() {
        return Util.fromLong(finishesAt());
    }

    record Clock ( Integer increment, Integer limit)  {}
    record Perf ( String icon, String key, String name, Integer position) {}

    sealed interface Pos permits Pos.None, Pos.Position {
        static None none = new None();
        default void ifPresent(Consumer<Position> pos) { if (this instanceof Position p) pos.accept(p); }
        record None() implements Pos {}
        record Position(String eco, String name, String wikiPath, String fen) implements Pos {}
    }

    record Scheduled(
        String id,
        String createdBy,
        String system,
        Integer minutes,
        Clock clock,
        boolean rated,
        String fullName,
        Integer nbPlayers,
        Variant variant,
        Long startsAt,
        Long finishesAt,
        TournamentState status,
        Perf perf,
        Pos position,
        boolean hasMaxRating,
        Long secondsToStart,
        LightUser winner,

        Schedule schedule

        ) implements Tournament {

            public Scheduled {
                position = position == null ? Pos.none : position;
            }
            public record Schedule (String freq, String speed) {}
        }


    record LocalArena(
        String id,
        String createdBy,
        String system,
        Integer minutes,
        Clock clock,
        boolean rated,
        String fullName,
        Integer nbPlayers,
        Variant variant,
        Long startsAt,
        Long finishesAt,
        TournamentState status,
        Perf perf,
        Pos position,
        boolean hasMaxRating,
        Long secondsToStart,
        LightUser winner

        ) implements Tournament {
            public LocalArena {
                position = position == null ? Pos.none : position;
            }
        }

    record TeamBattle(
        String id,
        String createdBy,
        String system,
        Integer minutes,
        Clock clock,
        boolean rated,
        String fullName,
        Integer nbPlayers,
        Variant variant,
        Long startsAt,
        Long finishesAt,
        TournamentState status,
        Perf perf,
        boolean hasMaxRating,
        Long secondsToStart,
        LightUser winner,

        Teams teamBattle

        ) implements Tournament {
            public record Teams (List<String> teams, Integer nbLeaders) {}
        }

}
