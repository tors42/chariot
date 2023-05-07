package chariot.model;

import java.util.List;
import java.util.Optional;

public record PerformanceStatistics(Perf perf, Integer rank, Float percentile, Stat stat)  {

    public record Perf(Glicko glicko, Integer nb, Integer progress) { }
    public record Glicko(Float rating, Float deviation, boolean provisional) {}

    public record PerfType(String key, String name) {}
    public record DateResult( Integer value, /* "int" */ String at, String gameId) {}

    public record OpId(String id, String name, Optional<String> title) {}

    public record Result(Integer opInt, OpId opId, String at, String gameId) { }
    public record Results(List<Result> results) { }
    public record Count(Integer all, Integer rated, Integer win, Integer loss, Integer draw,
            Integer tour, Integer berserk, Integer opAvg, Integer seconds, Integer disconnects) {}

    public record ResultStreak(Streak win, Streak loss) {}
    public record PlayStreak(Streak nb, Streak time, String lastDate) {}

    public record Streak(StreakInfo cur, StreakInfo max) {}
    public record StreakPoint(String at, String gameId) {}

    public sealed interface StreakInfo permits WithoutStreak, WithStreak { }
    public record WithoutStreak(Integer v) implements StreakInfo {}
    public record WithStreak(Integer v, StreakPoint from, StreakPoint to) implements StreakInfo { }

    public record Stat(
            PerfType perfType,
            DateResult highest,
            DateResult lowest,
            Results bestWins,
            Results worstLosses,
            Count count,
            ResultStreak resultStreak,
            PlayStreak playStreak
            ) { }
}
