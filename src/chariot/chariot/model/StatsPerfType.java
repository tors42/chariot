package chariot.model;

public enum StatsPerfType {

    // "game" stats for "standard", with separate time controls
    ultraBullet, bullet, blitz, rapid, classical, correspondence,

    // "game" stats for variants, with shared time controls
    antichess, atomic,  chess960,  crazyhouse, horde, kingOfTheHill, racingKings, threeCheck, 

    // "game" stats for puzzles
    puzzle,

    // "puzzle run" stats,
    storm, streak, racer;
}


