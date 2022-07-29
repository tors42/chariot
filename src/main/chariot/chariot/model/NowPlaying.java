package chariot.model;

import java.util.List;

import chariot.model.Enums.Color;

/**
 * @deprecated See {@link chariot.model.PlayingWrapper}
 */
@Deprecated
public record NowPlaying(List<Game> nowPlaying)  {
    /**
     * @deprecated See {@link chariot.model.GameInfo}
     */
    @Deprecated
    public record Game(
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
            Opponent opponent) {}
    public record Variant(String key, String name) {}
    public record Opponent(String id, String username, Integer rating) {}
}
