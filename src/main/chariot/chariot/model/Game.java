package chariot.model;

import java.time.ZonedDateTime;
import java.util.List;

import chariot.model.Enums.*;
import static chariot.internal.Util.orEmpty;

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

    // Optional...
    String moves,
    String initialFen,
    String lastFen,
    String pgn,
    String tournament,
    String swiss,
    Integer daysPerTurn,
    Color winner,
    Opening opening,
    Clock clock,
    List<Integer> clocks,
    List<AnalysisEntry> analysis,
    Opt<Division> division
    )  {
    public Game {
        moves = orEmpty(moves);
        initialFen = orEmpty(initialFen);
        lastFen = orEmpty(lastFen);
        pgn = orEmpty(pgn);
        tournament = orEmpty(tournament);
        swiss = orEmpty(swiss);
        daysPerTurn = daysPerTurn == null ? 0 : daysPerTurn;
    }

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
