package chariot.model;

public record LeaderboardEntry(String name, double score, int played, Opt<Integer> rating, Opt<String> title, Opt<Integer> fideId, Opt<String> fed) {}
