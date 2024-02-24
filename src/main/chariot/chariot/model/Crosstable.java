package chariot.model;

public record Crosstable(Results results, Opt<Results> matchup) {

    public record Results(Result user1, Result user2, int nbGames) {
        public double pointsForUser(String userId) {
            return switch(this) {
                case Results(var u1, var u2, var __) when u1.id().equals(userId) -> u1.points;
                case Results(var u1, var u2, var __) when u2.id().equals(userId) -> u2.points;
                default -> 0;
            };
        }
    }
    public record Result(String id, double points) {}

    public double pointsForUser(String userId) {
        return results.pointsForUser(userId);
    }
}
