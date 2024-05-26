package chariot.model;

import java.time.Duration;
import java.util.List;

import chariot.model.Enums.Color;

public sealed interface TVFeedEvent permits TVFeedEvent.Featured, TVFeedEvent.Fen {
    public record Featured(String id, Color orientation, List<PlayerInfo> players, String fen) implements TVFeedEvent {}
    public record Fen(String fen, String lastMove, Duration whiteTime, Duration blackTime) implements TVFeedEvent {}

    public record PlayerInfo(UserInfo user, Color color, int rating, Duration time) {}
}
