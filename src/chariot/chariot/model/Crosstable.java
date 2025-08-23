package chariot.model;

public record Crosstable(Results results, Opt<Results> matchup) {

    public record Results(Result user1, Result user2, int nbGames) {
        public double pointsForUser(String userId) {
            return switch(this) {
                case Results(Result(var id1, var p1),_,_) when id1.equals(userId) -> p1;
                case Results(_,Result(var id2, var p2),_) when id2.equals(userId) -> p2;
                default -> 0;
            };
        }
    }
    public record Result(String id, double points) {}

    public double pointsForUser(String userId) {
        return results.pointsForUser(userId);
    }
}
