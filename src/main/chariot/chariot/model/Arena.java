package chariot.model;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public record Arena(
        TourInfo tourInfo,
        Duration duration,
        boolean berserkable,
        ConditionInfo<ArenaCondition> conditions,
        List<Standing> standings,
        List<Podium> podium,
        List<TeamStanding> teamStandings,
        List<TopGame> topGames,
        boolean pairingsClosed,
        boolean isRecentlyFinished,
        Opt<String> spotlight,
        Opt<Battle> teamBattle,
        Opt<Quote> quote,
        Opt<GreatPlayer> greatPlayer,
        Opt<Featured> featured,
        Opt<Stats> stats
        ) {

    public String id() { return tourInfo.id(); }

    public record Battle(List<TeamInfo> teams, int leaders) {}
    public record TeamInfo(String id, String name, Opt<String> flair) {}

    public record Standing(LightUser user,
            int rank, int rating, boolean provisional, int score, String sheet,
            boolean fire, boolean paused, Opt<String> team) {}

    public record Podium(LightUser user,
            int rank, int rating, int score, int performance,
            int games, int berserk, int win,
            Opt<String> team) {}

    public record TeamStanding(int rank, String teamId, int score, Map<LightUser, Integer> players) {}

    public record Featured(String gameId, String fen, Enums.Color orientation, Enums.Color color, String lastMove,
            LightUser white, int whiteRating, int whiteRank, boolean whiteBerserk,
            LightUser black, int blackRating, int blackRank, boolean blackBerserk,
            Duration whiteTimeLeft, Duration blackTimeLeft, Opt<Enums.Color> winner) { }

    public record TopGame(String gameId,
            String whiteName, int whiteRating, int whiteRank,
            String blackName, int blackRating, int blackRank,
            Opt<String> whiteTeamId, Opt<String> blackTeamId) {}

    public record Stats(int games, int whiteWins, int blackWins, int draws, int averageRating, int berserks, int moves) {}

    public record Quote(String text, String author) {}
    public record GreatPlayer (String name, URI url) {}
}
