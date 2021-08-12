package chariot.api;

import chariot.model.Result;
import chariot.model.Puzzle;
import chariot.model.StormDashboard;

public interface Puzzles {

    Result<StormDashboard> stormDashboard(String username);
    Result<StormDashboard> stormDashboard(String username, int days);

    Result<Puzzle>         dailyPuzzle();
}
