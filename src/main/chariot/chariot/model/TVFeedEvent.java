package chariot.model;

import java.util.List;

import chariot.model.Enums.Color;

public record TVFeedEvent(String t, Data d) {
    public sealed interface Data permits Featured, Fen {}

    public record Featured(String id, Color orientation, List<PlayerInfo> players, String fen) implements Data {}
    public record Fen(String fen, String lm, Integer wc, Integer bc) implements Data {}

    public record PlayerInfo(LightUser user, Color color, Integer rating, Integer seconds) {}
}
