package chariot.model;

import java.util.List;

import chariot.model.Enums.Color;

public record TVFeed (String t, Data d)  {

    public sealed interface Data permits Featured, Fen {}

    public record Featured (String id, Color orientation, List<Player> players, String fen) implements Data {
        public record Player (LightUser user, Color color, Integer rating) {}
    }

    public record Fen(String fen, String lm, Integer wc, Integer bc) implements Data {}
}
