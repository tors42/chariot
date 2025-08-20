package chariot.model;

public record UserTopAll (
    java.util.List<UserPerformance> bullet,
    java.util.List<UserPerformance> blitz,
    java.util.List<UserPerformance> rapid,
    java.util.List<UserPerformance> classical,
    java.util.List<UserPerformance> ultraBullet,
    java.util.List<UserPerformance> chess960,
    java.util.List<UserPerformance> crazyhouse,
    java.util.List<UserPerformance> antichess,
    java.util.List<UserPerformance> atomic,
    java.util.List<UserPerformance> horde,
    java.util.List<UserPerformance> kingOfTheHill,
    java.util.List<UserPerformance> racingKings,
    java.util.List<UserPerformance> threeCheck
    )  {}
