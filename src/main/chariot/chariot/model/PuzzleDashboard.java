package chariot.model;

import java.util.Map;

public record PuzzleDashboard(int days, PuzzleResult global, Map<String, Theme> themes) implements Model {

    public record PuzzleResult(int firstWins, int nb, int performance, int puzzleRatingAvg, int replayWins) {}
    public record Theme(PuzzleResult results, String theme) {}

}
