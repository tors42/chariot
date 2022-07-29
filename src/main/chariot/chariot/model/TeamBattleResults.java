package chariot.model;

import java.util.List;

public record TeamBattleResults (
        String id,
        List<Teams> teams
        )  {

    public record Teams (Integer rank, String id, Integer score, List<Player> players) {
        public record Player (User user, String id, Integer score) {
            public record User (String name, String id) {}
        }
    }
}
