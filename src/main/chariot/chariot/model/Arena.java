package chariot.model;

import java.util.List;
import java.util.Map;

import chariot.model.Enums.Color;
import chariot.model.Enums.VariantName;
import static chariot.internal.Util.orEmpty;

public sealed interface Arena extends Model {

    String id();
    String createdBy();
    String startsAt();
    String system();
    String fullName();
    Integer minutes();
    Perf perf();
    Clock clock();
    VariantName variant();
    boolean rated();
    boolean berserkable();
    Verdict verdicts();
    Integer nbPlayers();
    List<Duel> duels();
    Standing standing();

    record Upcoming(
        String id, String createdBy, String startsAt, String system, String fullName, Integer minutes, Perf perf, Clock clock, VariantName variant, boolean rated, boolean berserkable, Verdict verdicts, Integer nbPlayers, List<Duel> duels, Standing standing,

        Integer secondsToStart,
        GreatPlayer greatPlayer

        ) implements Arena { }


    record Ongoing(
        String id, String createdBy, String startsAt, String system, String fullName, Integer minutes, Perf perf, Clock clock, VariantName variant, boolean rated, boolean berserkable, Verdict verdicts, Integer nbPlayers, List<Duel> duels, Standing standing,

        boolean isStarted,
        Integer secondsToFinish,
        Featured featured

        ) implements Arena {
            public record Featured(String id, String fen, String orientation, Color color, String lastMove, Player white, Player black, String winner) {
                public record Player(int rank, String name, int rating) {}
            }
        }

    record Finished(
        String id, String createdBy, String startsAt, String system, String fullName, Integer minutes, Perf perf, Clock clock, VariantName variant, boolean rated, boolean berserkable, Verdict verdicts, Integer nbPlayers, List<Duel> duels, Standing standing,

        boolean isFinished,
        boolean isRecentlyFinished,
        boolean pairingsClosed, // When isRecentlyFinished=true vanishes, a pairingsClosed=true shows up...
        List<Podium> podium,
        Stats stats

        ) implements Arena {

        public record Podium (String name, Integer rank, Integer rating, Integer score, Sheet sheet, String team, NB nb, Integer performance) {
            public Podium {
                team = orEmpty(team);
            }
            public record NB (Integer game, Integer berserk, Integer win) {}
        }

        public record Stats ( Integer games, Integer moves, Integer whiteWins, Integer blackWins, Integer draws, Integer berserks, Integer averageRating)  {}
    }


    record TeamUpcoming(
        String id, String createdBy, String startsAt, String system, String fullName, Integer minutes, Perf perf, Clock clock, VariantName variant, boolean rated, boolean berserkable, Verdict verdicts, Integer nbPlayers, List<Duel> duels, Standing standing,

        Integer secondsToStart,
        GreatPlayer greatPlayer,

        List<TeamStanding> teamStanding,
        DuelTeams duelTeams,
        TeamBattle teamBattle

        ) implements Arena {}


    record TeamOngoing(
        String id, String createdBy, String startsAt, String system, String fullName, Integer minutes, Perf perf, Clock clock, VariantName variant, boolean rated, boolean berserkable, Verdict verdicts, Integer nbPlayers, List<Duel> duels, Standing standing,

        boolean isStarted,
        Integer secondsToFinish,
        Featured featured,

        List<TeamStanding> teamStanding,
        DuelTeams duelTeams,
        TeamBattle teamBattle

        ) implements Arena {
            public record Featured(String id, String fen, String orientation, String color, String lastMove, Player white, Player black, String winner) {
                public record Player(int rank, String name, int rating) {}
            }
        }

    record TeamFinished(
        String id, String createdBy, String startsAt, String system, String fullName, Integer minutes, Perf perf, Clock clock, VariantName variant, boolean rated, boolean berserkable, Verdict verdicts, Integer nbPlayers, List<Duel> duels, Standing standing,

        boolean isFinished,
        boolean isRecentlyFinished,
        boolean pairingsClosed, // When isRecentlyFinished=true vanishes, a pairingsClosed=true shows up...
        List<Podium> podium,
        Stats stats,

        List<TeamStanding> teamStanding,
        DuelTeams duelTeams,
        TeamBattle teamBattle

        ) implements Arena {

        public record Podium (String name, Integer rank, Integer rating, Integer score, Sheet sheet, String team, NB nb, Integer performance) {
            public Podium {
                team = orEmpty(team);
            }
            public record NB (Integer game, Integer berserk, Integer win) {}
        }

        public record Stats ( Integer games, Integer moves, Integer whiteWins, Integer blackWins, Integer draws, Integer berserks, Integer averageRating)  {}
    }

    public record Sheet (String scores, boolean fire) {}

    public record Duel (String id, List<P> p) {
        public record P (String n, Integer r, Integer k) {}
    }

    public record Standing (Integer page, List<Player> players) {
        public record Player (String name, Integer rank, Integer rating, Integer score, Sheet sheet, String team, boolean withdraw) {
            public Player {
                team = orEmpty(team);
            }
        }
    }

    public record GreatPlayer (String name, String url) {}

    // condition - member of team, minimum rating etc
    public record Verdict (List<Condition> list, boolean accepted) {
        public record Condition(String condition, String verdict) {}
    }

    public record Clock (Integer increment, Integer limit) {}
    public record Perf (String icon, String key, String name) {}

    public record TeamStanding (Integer rank, String id, Integer score, List<Player> players) {
        public record Player (User user, Integer score) {
            public record User (String name, String id) {}
        }
    }

    public record DuelTeams() {}

    public record TeamBattle (Map<String,String> teams, Integer nbLeaders) {}

}
