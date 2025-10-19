package chariot.model;

import module java.base;

public record BroadcastPlayer(
            String name,
            Opt<String> title,
            Opt<Integer> rating,
            Opt<Integer> fideId,
            Opt<String> team,
            Opt<String> fed,
            int played,
            Opt<Float> score,
            Opt<Integer> ratingDiff,
            Opt<Integer> performance,
            Opt<Tiebreak[]> tiebreaks,
            Opt<Integer> rank,
            Opt<Fide> fide,
            Opt<Boolean> isFollowing,
            Opt<Game[]> games
            ) {

    // Opt<List<X>> is tricky for YayMapper, handling parameterized types...
    // Opt<X[]> works like a charm. So taking easy way out and provide list getters...
    public List<Game> gamesList() { return games().map(Arrays::asList).orElse(List.of()); }
    public List<Tiebreak> tiebreaksList() { return tiebreaks().map(Arrays::asList).orElse(List.of()); }

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
            Opt<Float> customPoints,
            Opt<Integer> ratingDiff) {}
}
