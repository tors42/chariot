package chariot.api;

import chariot.model.Result;
import chariot.model.PuzzleActivity;
import chariot.model.PuzzleDashboard;

public interface PuzzlesAuth extends Puzzles {

    /**
     * @param max How many days of history to return [ 0 .. 365 ]
     */
    Result<PuzzleActivity> activity(int max);
    Result<PuzzleActivity> activity();


    /**
     * @param days How many days to look back when aggregating puzzle results. 30 is sensible.
     */
    public Result<PuzzleDashboard> puzzleDashboard(int days);

}
