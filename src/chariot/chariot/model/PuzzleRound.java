package chariot.model;

import module java.base;

public record PuzzleRound(List<Puzzle> puzzles, List<Round> rounds) {
    public record Round(String id, boolean win, int ratingDiff) {}
    public record Solution(String id, boolean win, boolean rated) {}
}
