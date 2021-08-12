package chariot.model;

import chariot.model.Enums.Color;

public record NowPlaying(
        String fullId,
        String gameId,
        String fen,
        Color color,
        String lastMove,
        String speed,
        String perf,
        boolean rated,
        boolean isMyTurn,
        Variant variant,
        Opponent opponent) implements Model {

    public record Variant(String key, String name) {}
    public record Opponent(String id, String username, Integer rating) {}
}
