package chariot.model;

public record ArenaResult(
        int rank,
        int score,
        int rating,
        String username,
        Opt<String> title,
        Opt<String> flair,
        Opt<Integer> performance,
        Opt<String> team,
        Opt<String> sheet)  {
}
