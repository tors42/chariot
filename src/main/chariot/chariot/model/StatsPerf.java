package chariot.model;

public sealed interface StatsPerf {

    // initially created: blitz, bullet, correspondence, classical, rapid
    // if played:         puzzle, antichess, atomic, horde, threeCheck, crazyhouse, etc
    record StatsPerfGame(int games, int rating, int rd, int prog, boolean prov) implements StatsPerf {}

    // storm, streak, racer
    record StatsPerfRun(int runs, int score) implements StatsPerf {}

}
