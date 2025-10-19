package chariot.model;

import java.util.List;

import chariot.model.Enums.Color;

public record Puzzle(GameInfo game, PuzzleInfo puzzle)  {
    public record GameInfo(String id, String clock, String pgn, boolean rated, Perf perf, List<Player> players) {}
    public record Perf(String name, String key) {}
    public record Player(Color color, String name, String id, Opt<Integer> patronColor, int rating, Opt<String> title, Opt<String> flair) {
        public boolean patron() { return patronColor.isPresent(); }
    }
    public record PuzzleInfo(String id, int initialPly, int rating, int plays, List<String> solution, List<String> themes) {}
}
