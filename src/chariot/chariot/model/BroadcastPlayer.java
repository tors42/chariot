package chariot.model;

import module java.base;

public record BroadcastPlayer(
            String name,
            Opt<String> title,
            Opt<Integer> fideId,
            Opt<String> team,
            Opt<String> fed,
            int played,
            Opt<Float> score,
            Opt<Ratings> ratingsMap,
            Opt<Ratings> ratingDiffs,
            Opt<Ratings> performances,
            List<Tiebreak> tiebreaks,
            Opt<Integer> rank,
            Opt<Boolean> isFollowing,
            List<Game> games
            ) {

    public BroadcastPlayer {
        if (tiebreaks == null) tiebreaks = List.of();
        if (games == null) games = List.of();
    }

    public record Tiebreak(String extendedCode, String description, float points) {}
    public record Fide(Ratings ratings, Opt<Integer> year) {}
    public record Ratings(Opt<Integer> standard, Opt<Integer> rapid, Opt<Integer> blitz) {}
    public record Opponent(
            String name,
            Opt<String> title,
            Opt<Integer> rating,
            Opt<Integer> fideId,
            Opt<String> fed
            ) {}
    public record Game(
            String round,
            String id,
            Opponent opponent,
            Enums.Color color,
            String points,
            boolean ongoing,
            Opt<String> fideTC,
            Opt<Float> customPoints,
            Opt<Integer> ratingDiff) {}
}
