package chariot.model;

import java.time.ZonedDateTime;
import java.util.List;

import chariot.model.Enums.*;

public record Game (
    String id,
    boolean rated,
    GameVariant variant,
    String speed,
    String perf,
    String source,
    ZonedDateTime createdAt,
    ZonedDateTime lastMoveAt,
    Status status,
    Players players,

    Opt<String> moves,
    Opt<String> initialFen,
    Opt<String> lastFen,
    Opt<String> pgn,
    Opt<String> tournament,
    Opt<String> swiss,
    Opt<Integer> daysPerTurn,
    Opt<Color> winner,
    Opt<Opening> opening,
    Opt<Clock> clock,
    List<Integer> clocks,
    List<AnalysisEntry> analysis,
    Opt<Division> division,
    Opt<Boolean> bookmarked
    )  {

    public record Players (Player white, Player black) { }

    public record Opening (String eco, String name, Integer ply) {}

    public record Clock (int initial, int increment, int totalTime) {}


    public sealed interface AnalysisEntry permits
        Eval,
        ForcedMate,
        AnnotatedEval {}

    /**
     * @param eval Evaluation in centipawns
     */
    public record Eval(int eval) implements AnalysisEntry {}

    /**
     * @param mate Number of moves for mate
     */
    public record ForcedMate(int mate) implements AnalysisEntry {}

    /**
     * @param eval Evaluation in centipawns
     * @param best Best move in UCI notation
     * @param variation Best variation in SAN notation
     * @param judgment Judgment annotation
     */
    public record AnnotatedEval(int eval, String best, String variation, Judgment judgment) implements AnalysisEntry {}

    public record Judgment(Severity name, String comment) {}

    public enum Severity {
        Inaccuracy,
        Mistake,
        Blunder
    }

    public sealed interface Division {}
    public record Middle(int middle)             implements Division {}
    public record MiddleEnd(int middle, int end) implements Division {}
}
