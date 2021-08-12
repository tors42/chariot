package chariot.model;

import java.util.List;

import chariot.model.Enums.Color;

import java.time.ZonedDateTime;

public record Activity(
        Interval interval,
        Games games,
        Puzzles puzzles,
        Tournaments tournaments,
        List<Practice> practice,
        CorrespondenceMoves correspondenceMoves,
        CorrespondenceEnds correspondenceEnds,
        Follows follows,
        List<Teams> teams,
        List<Posts> posts
        ) implements Model {

    public record Interval(ZonedDateTime start, ZonedDateTime end) {};

    public record Result(String name, Integer win, Integer loss, Integer draw, RP rp) {
        public record ResultHelper(Integer win, Integer loss, Integer draw, Activity.Result.RP rp) {} // check if this can be moved into chariot.internal.yay.Mapper (got IllegalAccess)
        public record RP(Integer before, Integer after) {};
    }
    public record Games(List<Result> result) {};
    public record Puzzles(Result score) {};
    public record Tournaments(Integer nb, List<Tournament> best) {
        public record Tournament(Info tournament, Integer nbGames, Integer score, Integer rank, Integer rankPercent) {
            public record Info(String id, String name) {}
        }
    }

    public record Practice(String url, String name, Integer nbPositions) {}

    public record Game(String id, Color color, String url, String variant, String speed, String perf, boolean rated, Opponent opponent) {
        public record Opponent(String user, Integer rating) {}
    }
    public record CorrespondenceMoves(Integer nb, List<Game> games) {}
    public record CorrespondenceEnds(Result score, List<Game> games) {}

    public record Follows(List<String> inIds, List<String> outIds) {}
    public record Teams(String url, String name) {}
    public record Posts(String topicUrl, String topicName, List<Post> posts) {
        public record Post(String url, String text) {}
    }

}
