package chariot.model;

import chariot.model.Enums.Color;

public record GameInfo(
            String fullId,
            String gameId,
            String fen,
            Color color,
            String lastMove,
            String source,
            String speed,
            String perf,
            Integer secondsLeft,
            boolean rated,
            boolean isMyTurn,
            boolean hasMoved,
            Variant variant,
            Opponent opponent) implements Model {
    public record Variant(String key, String name) {}
    public record Opponent(String id, String username, Integer rating) {}
}

