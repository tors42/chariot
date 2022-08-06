package chariot.api;

import java.util.function.Consumer;

import chariot.model.*;

public interface Puzzles {

    One<StormDashboard> stormDashboard(String username, Consumer<PuzzleParams> params);
    default One<StormDashboard> stormDashboard(String username) { return stormDashboard(username, __ -> {}); }
    default One<StormDashboard> stormDashboard(String username, int days) { return stormDashboard(username, p -> p.days(days)); }

    One<Puzzle>         dailyPuzzle();

    interface PuzzleParams {
        PuzzleParams days(int days);
    }
}
