package chariot.api;

import chariot.model.*;

public interface PuzzlesAuth extends Puzzles {

    /**
     * @param max How many days of history to return [ 0 .. 365 ]
     */
    Many<PuzzleActivity> activity(int max);
    Many<PuzzleActivity> activity();

    /**
     * Create and join a Puzzle Race
     * @return the id and the URL for the Puzzle Race
     */
    One<PuzzleRace> createAndJoinRace();

    /**
     * @param days How many days to look back when aggregating puzzle results. 30 is sensible.
     */
    One<PuzzleDashboard> puzzleDashboard(int days);

}
