package chariot.model;

import java.util.List;
import java.util.Set;

import chariot.model.Enums.Color;
import chariot.model.Enums.VariantName;
import static chariot.internal.Util.orEmpty;

import java.time.ZonedDateTime;

public record Activity(Interval interval, Set<Activity.Type> activities) implements Model {

    public record Interval(ZonedDateTime start, ZonedDateTime end) {};

    public sealed interface Type permits Type.Games, Type.Puzzles, Type.Tournaments, Type.Practices, Type.CorrespondenceMoves, Type.CorrespondenceEnds, Type.Follows, Type.Teams, Type.Posts, Type.Simuls, Type.Unknown {
        public record Games(List<Result> result) implements Type {};
        public record Puzzles(Result score) implements Type {};
        public record Tournaments(Integer nb, List<Tournament> best) implements Type {}
        public record Practices(List<Practice> practises) implements Type {}
        public record CorrespondenceMoves(Integer nb, List<Game> games) implements Type {}
        public record CorrespondenceEnds(Result score, List<Game> games) implements Type {}
        public record Follows(List<String> inIds, List<String> outIds) implements Type {}
        public record Teams(List<Team> teams) implements Type {}
        public record Posts(List<Topic> topics) implements Type {}
        public record Simuls(List<Simul> simuls) implements Type {}
        public record Unknown(String name, String raw) implements Type {}
    }

    public record Result(String name, Integer win, Integer loss, Integer draw, RP rp) {
        public record ResultHelper(Integer win, Integer loss, Integer draw, Activity.Result.RP rp) {}
        public record RP(Integer before, Integer after) {};
    }

    public record Tournament(Info tournament, Integer nbGames, Integer score, Integer rank, Integer rankPercent) {
        public record Info(String id, String name) {}
    }

    public record Practice(String url, String name, Integer nbPositions){}

    public record Game(String id, Color color, String url, String variant, String speed, String perf, Boolean rated, Opponent opponent) {
        public Game {
            variant = orEmpty(variant);
            speed = orEmpty(speed);
            perf = orEmpty(perf);
        }
        public record Opponent(String user, Integer rating) {}
    }

    public record Team(String url, String name) {}

    public record Topic(String topicUrl, String topicName, List<Post> posts) {
        public record Post(String url, String text) {}
    }

    public record Simul(String id, String name, boolean isHost, List<VariantName> variants, Score score) {
        public record Score(int win, int loss, int draw) {}
    }
}
