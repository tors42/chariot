package chariot.model;

import static chariot.internal.Util.orEmpty;

public record ArenaResult(
        Integer rank,
        Integer score,
        Integer rating,
        String username,
        String title,
        Integer performance,
        String team,
        String sheet)  {

    public ArenaResult {
        title = orEmpty(title);
        team = orEmpty(team);
        sheet = orEmpty(sheet);
    }

}
