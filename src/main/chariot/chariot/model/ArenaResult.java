package chariot.model;

import static chariot.internal.Util.orEmpty;

public record ArenaResult(
        Integer rank,
        Integer score,
        Integer rating,
        String username,
        String title,
        Integer performance,
        String team) implements Model {

    public ArenaResult {
        title = orEmpty(title);
        team = orEmpty(team);
    }

}
