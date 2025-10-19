package chariot.api;

import module java.base;
import module chariot;

public interface PuzzlesApi {

    One<StormDashboard> stormDashboard(String username, Consumer<StormDashboardParams> params);
    default One<StormDashboard> stormDashboard(String username) { return stormDashboard(username, _ -> {}); }
    default One<StormDashboard> stormDashboard(String username, int days) { return stormDashboard(username, p -> p.days(days)); }

    One<Puzzle>         dailyPuzzle();
    One<Puzzle>         byId(String puzzleId);
    One<Puzzle>         nextPuzzle(Consumer<PuzzleParams> params);
    default One<Puzzle> nextPuzzle() { return nextPuzzle(_ -> {}); }

    Many<Puzzle>        batch(Consumer<PuzzleNbParams> params);
    One<PuzzleRound>    batchSolve(List<PuzzleRound.Solution> solutions, Consumer<PuzzleNbSolveParams> params);
    default One<PuzzleRound> batchSolve(List<PuzzleRound.Solution> solutions) { return batchSolve(solutions, _ -> {}); }

    One<PuzzleRaceResult> raceResult(String id);

    interface StormDashboardParams {
        StormDashboardParams days(int days);
    }

    interface PuzzleParams extends CommonParams<PuzzleParams> {}
    interface PuzzleNbParams extends CommonParams<PuzzleNbParams> {
        /// @param nb How many puzzles to fetch. `[1 .. 50]` Default: `15`
        PuzzleNbParams nb(int nb);
    }

    interface PuzzleNbSolveParams extends CommonParams<PuzzleNbSolveParams> {
        /// @param nb How many puzzles to fetch. `[0 .. 50]` Default: `0`
        PuzzleNbSolveParams nb(int nb);
    }


    interface CommonParams<T> {
        T theme(PuzzleAngle theme);
        T color(Enums.Color color);
        default T theme(PuzzleAngle.Theme theme) { return theme((PuzzleAngle) theme); }
        default T opening(String opening) { return theme(PuzzleAngle.provider().opening(opening)); }
        default T theme(Function<PuzzleAngle.Provider, PuzzleAngle> theme) { return theme(theme.apply(PuzzleAngle.provider())); }
        default T color(Function<Enums.Color.Provider, Enums.Color> color) { return color(color.apply(Enums.Color.provider())); }

        T difficulty(Difficulty difficulty);
        default T difficulty(Function<Difficulty.Provider, Difficulty> difficulty) { return difficulty(difficulty.apply(Difficulty.provider())); }
    }

    enum Difficulty {
        easiest,
        easier,
        normal,
        harder,
        hardest,
        ;

        public interface Provider {
            default Difficulty easiest() { return Difficulty.easiest; }
            default Difficulty easier() { return Difficulty.easier; }
            default Difficulty normal() { return Difficulty.normal; }
            default Difficulty harder() { return Difficulty.harder; }
            default Difficulty hardest() { return Difficulty.hardest; }
        }
        static Provider provider() {return new Provider(){};}
    }

}
