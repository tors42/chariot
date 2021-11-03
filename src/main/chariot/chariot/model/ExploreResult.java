package chariot.model;

import java.util.List;

import chariot.model.Enums.Color;

public record ExploreResult(
        int white,
        int draws,
        int black,
        List<Move> moves,
        List<Game> topGames,
        List<Game> recentGames,
        Opening opening) implements Model {

    public record Move(
            String uci,
            String san,
            int white,
            int draws,
            int black,
            int averageRating,
            Game game) {}

    public record Game(
            String uci,
            String id,
            Color winner,
            Player white,
            Player black,
            int year,
            String month) {

        public record Player(
                String name,
                Integer rating) {}
    }

    public record Opening(String eco, String name) {}

}
