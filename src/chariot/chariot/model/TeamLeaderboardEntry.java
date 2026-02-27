package chariot.model;

import module java.base;

public record TeamLeaderboardEntry(
        String name,
        float mp,
        float gp,
        List<Match> matches,
        List<BroadcastPlayer> players,
        Opt<Integer> averageRating
        ) {

    public record Match(
            String roundId,
            String opponent,
            Opt<String> points,
            Opt<Float> mp,
            Opt<Float> gp
            ) {}
}
