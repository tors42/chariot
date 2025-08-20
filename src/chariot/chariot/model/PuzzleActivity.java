package chariot.model;

import java.time.ZonedDateTime;
import java.util.List;

public record PuzzleActivity(ZonedDateTime date, boolean win, Puzzle puzzle)  {
    public record Puzzle(String id, int rating, int plays, List<String> solution, List<String> themes, String fen, String lastMove) {}
}
