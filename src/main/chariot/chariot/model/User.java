package chariot.model;

import chariot.internal.Util;
import static chariot.internal.Util.orEmpty;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @param id The user id (Username in lowercase)
 * @param username The username
 */
public record User(
        String id,
        String username,
        String url,
        boolean disabled,
        boolean closed,
        boolean tosViolation,
        boolean booster,
        boolean patron,
        boolean streaming,
        boolean followable,
        boolean following,
        boolean blocking,
        boolean followsYou,
        boolean verified,
        Optional<String> title,
        Optional<String> playing,
        Optional<Profile> profile,
        List<Trophy> trophies,
        Long createdAt,
        Long seenAt,
        PlayTime playTime,
        Count count,
        Perfs perfs
        )  {

    public User {
        url = orEmpty(url);
        trophies = trophies == null ? List.of() : trophies;
    }

    public java.time.ZonedDateTime createdTime() {
        return Util.fromLong(createdAt());
    }

    public java.time.ZonedDateTime seenTime() {
        return Util.fromLong(seenAt());
    }

    public record Profile(
            String country,
            String location,
            String bio,
            String firstName,
            String lastName,
            String links,
            Integer fideRating,
            Integer uscfRating,
            Integer ecfRating
            ) {

        public Profile {
            country = orEmpty(country);
            location = orEmpty(location);
            bio = orEmpty(bio);
            firstName = orEmpty(firstName);
            lastName = orEmpty(lastName);
            links = orEmpty(links);
        }

    }

    public record PlayTime(Long total, Long tv) {}

    public record Count(
                Integer all,
                Integer rated,
                Integer ai,
                Integer draw,
                Integer drawH,
                Integer loss,
                Integer lossH,
                Integer win,
                Integer winH,
                Integer bookmark,
                Integer playing,
                Integer imported, // "import"
                Integer me
                ) {}

    public record Perfs(
            Perf chess960,
            Perf atomic,
            Perf racingKings,
            Perf ultraBullet,
            Perf blitz,
            Perf kingOfTheHill,
            Perf bullet,
            Perf correspondence,
            Perf horde,
            Perf puzzle,
            Perf classical,
            Perf rapid,
            StormPerf storm
            ) {

        public Perfs {
            chess960 = orNone(chess960);
            atomic = orNone(atomic);
            racingKings = orNone(racingKings);
            ultraBullet = orNone(ultraBullet);
            blitz = orNone(blitz);
            kingOfTheHill = orNone(kingOfTheHill);
            bullet = orNone(bullet);
            correspondence = orNone(correspondence);
            horde = orNone(horde);
            puzzle = orNone(puzzle);
            classical = orNone(classical);
            rapid = orNone(rapid);
            storm = storm == null ? StormPerf.none : storm;
        }

        private static Perf orNone(Perf perf) {
            return perf == null ? Perf.none : perf;
        }

        public sealed interface Perf permits Perf.None, Perf.Stats {
            static None none = new None();
            default Optional<Stats> maybe() {
                return this instanceof Stats s ? Optional.of(s) : Optional.empty();
            }
            default void ifPresent(Consumer<Stats> consumer) { if (this instanceof Stats s) consumer.accept(s); }
            record None() implements Perf { }
            record Stats(Integer games, Integer rating, Integer rd, Integer prog, Boolean prov) implements Perf {}
        }

        public sealed interface StormPerf permits StormPerf.None, StormPerf.Stats {
            static None none = new None();
            default Optional<Stats> maybe() {
                return this instanceof Stats s ? Optional.of(s) : Optional.empty();
            }
            default void ifPresent(Consumer<Stats> consumer) { if (this instanceof Stats s) consumer.accept(s); }
            record None() implements StormPerf {}
            record Stats(Integer runs, Integer score) implements StormPerf {}
        }
    }
}
