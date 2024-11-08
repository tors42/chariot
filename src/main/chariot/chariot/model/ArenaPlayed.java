package chariot.model;

public record ArenaPlayed(
        ArenaLight tournament,
        int games,
        int score,
        int rank,
        Opt<Integer> performance)  {
}
