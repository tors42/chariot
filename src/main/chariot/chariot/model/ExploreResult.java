package chariot.model;

import java.util.List;

public record ExploreResult(
        Integer white,
        Integer draws,
        Integer black,
        Float averageRating,
        List<Move> moves,
        List<Game> topGames,
        List<Game> recentGames,
        String opening) implements Model {

    public record Move(
            String uci,
            String san,
            Integer white,
            Integer draws,
            Integer black,
            Float averageRating) {}

    public record Game(
            String id,
            String winner,
            Player white,
            Player black,
            Integer year,
            String speed) {

        public record Player(
                String name,
                Integer rating) {}
    }

}
