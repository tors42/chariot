package chariot.model;

import static chariot.internal.Util.orEmpty;

// Leaderboard
public record UserPerformance (
        String id,
        String username,
        String title,
        boolean online,
        Opt<Integer> patronColor,
        Perfs perfs
        )  {

    public boolean patron() { return patronColor.isPresent(); }

    public UserPerformance {
        title = orEmpty(title);
    }

    public sealed interface Perfs permits
        Perfs.UltraBullet,
        Perfs.Bullet,
        Perfs.Blitz,
        Perfs.Rapid,
        Perfs.Classical,
        Perfs.Chess960,
        Perfs.Crazyhouse,
        Perfs.Antichess,
        Perfs.Atomic,
        Perfs.Horde,
        Perfs.KingOfTheHill,
        Perfs.RacingKings,
        Perfs.ThreeCheck {
            record UltraBullet(     Perf ultraBullet    ) implements Perfs {}
            record Bullet(          Perf bullet         ) implements Perfs {}
            record Blitz(           Perf blitz          ) implements Perfs {}
            record Rapid(           Perf rapid          ) implements Perfs {}
            record Classical(       Perf classical      ) implements Perfs {}
            record Chess960(        Perf chess960       ) implements Perfs {}
            record Crazyhouse(      Perf crazyhouse     ) implements Perfs {}
            record Antichess(       Perf antichess      ) implements Perfs {}
            record Atomic(          Perf atomic         ) implements Perfs {}
            record Horde(           Perf horde          ) implements Perfs {}
            record KingOfTheHill(   Perf kingOfTheHill  ) implements Perfs {}
            record RacingKings(     Perf racingKings    ) implements Perfs {}
            record ThreeCheck(      Perf threeCheck     ) implements Perfs {}
        }

    public record Perf(Integer rating, Integer progress) {}
}
