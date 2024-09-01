package chariot.model;

import java.time.Duration;
import java.time.ZonedDateTime;

public record Swiss(
        TourInfo tourInfo,
        int round,
        int nbRounds,
        int nbOngoing,
        ConditionInfo<SwissCondition> conditions,
        Opt<ZonedDateTime> nextRoundAt,
        Opt<Duration> nextRoundIn,
        Opt<Stats> stats
        )  {

    public String id() { return tourInfo.id(); }

    public record Stats(int games, int whiteWins, int blackWins, int draws, int byes, int absences, int averageRating) {}
}
