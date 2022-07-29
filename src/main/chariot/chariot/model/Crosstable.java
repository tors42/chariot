package chariot.model;

import java.util.Optional;
import java.util.Set;

public record Crosstable(Results total, java.util.Optional<Results> matchup)  {

    public record Results(Set<Result> users, Integer nbGames) {
        public record Result(String user, Double points) {}
    }

    public record Points(Double total, Optional<Double> matchup) {}

    public Points pointsForUser(String user) {
        return new Points(total().users().stream().filter(r -> r.user().equals(user)).map(r -> r.points()).findAny().orElse(-1d),
                matchup().isPresent() ?
                matchup().get().users().stream().filter(r -> r.user().equals(user)).map(r -> r.points()).findAny() :
                Optional.empty());
    }
}
