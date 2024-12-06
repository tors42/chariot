package chariot.api;

import java.util.function.Consumer;
import java.util.function.Function;

import chariot.model.*;

public interface PuzzlesApi {

    One<StormDashboard> stormDashboard(String username, Consumer<StormDashboardParams> params);
    default One<StormDashboard> stormDashboard(String username) { return stormDashboard(username, __ -> {}); }
    default One<StormDashboard> stormDashboard(String username, int days) { return stormDashboard(username, p -> p.days(days)); }

    One<Puzzle>         dailyPuzzle();
    One<Puzzle>         byId(String puzzleId);
    One<Puzzle>         nextPuzzle(Consumer<PuzzleParams> params);
    default One<Puzzle> nextPuzzle() { return nextPuzzle(__ -> {}); }

    interface StormDashboardParams {
        StormDashboardParams days(int days);
    }

    interface PuzzleParams {
        PuzzleParams theme(PuzzleAngle theme);
        default PuzzleParams theme(PuzzleAngle.Theme theme) { return theme((PuzzleAngle) theme); }
        default PuzzleParams opening(String opening) { return theme(PuzzleAngle.provider().opening(opening)); }
        default PuzzleParams theme(Function<PuzzleAngle.Provider, PuzzleAngle> theme) { return theme(theme.apply(PuzzleAngle.provider())); }
    }
}
