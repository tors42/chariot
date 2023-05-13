package chariot.api;

import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import chariot.model.*;

public interface PuzzlesAuth extends Puzzles {

    /**
     * @param params filter the puzzle activity search. Example {@code params -> params.max(50).before(now -> now.minusDays(5))}
     */
    Many<PuzzleActivity> activity(Consumer<PuzzleActivityParams> params);
    default Many<PuzzleActivity> activity() { return activity(__ -> {}); }

    @Deprecated
    /**
     * @deprecated Use {@ link #activity(Consumer)}
     */
    default Many<PuzzleActivity> activity(int max) { return activity(params -> params.max(max)); }

    /**
     * Create and join a Puzzle Race
     * @return the id and the URL for the Puzzle Race
     */
    One<PuzzleRace> createAndJoinRace();

    /**
     * @param days How many days to look back when aggregating puzzle results. 30 is sensible.
     */
    One<PuzzleDashboard> puzzleDashboard(int days);

    interface PuzzleActivityParams {
        /**
         * @param max How many entries to download. Default all entries.
         */
        PuzzleActivityParams max(int max);

        /**
         * @param before Download entries before this timestamp. Defaults to now.
         */
        PuzzleActivityParams before(ZonedDateTime before);

        /**
         * @param now Download entries before this timestamp. Example: {@code now -> now.minusDays(5)}
         */
        default PuzzleActivityParams before(UnaryOperator<ZonedDateTime> now) {
            return before(now.apply(ZonedDateTime.now()));
        }
    }

}
