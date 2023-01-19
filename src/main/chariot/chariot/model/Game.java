package chariot.model;

import java.util.List;

import chariot.model.Enums.GameVariant;
import chariot.internal.Util;
import static chariot.internal.Util.orEmpty;

public record Game (
    String id,
    boolean rated,
    GameVariant variant,
    String speed,
    String perf,
    long createdTime,
    long lastMoveTime,
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
    Winner winner,
    Opening opening,
    Clock clock,
    List<AnalysisEntry> analysis
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

    public enum Status { created, started, aborted, mate, resign, stalemate, timeout, draw, outoftime, cheat, noStart, unknonwFinish, variantEnd }

    public enum Winner { white, black }

    public java.time.ZonedDateTime createdAt() {
        return Util.fromLong(createdTime());
    }

    public java.time.ZonedDateTime lastMoveAt() {
        return Util.fromLong(lastMoveTime());
    }

    public record Players (GameUser white, GameUser black) { }

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

}
