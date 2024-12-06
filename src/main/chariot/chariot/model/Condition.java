package chariot.model;

import java.time.Period;
import java.util.List;
import java.util.Locale;

import chariot.model.TourInfo.Speed;

public sealed interface Condition permits SwissCondition, ArenaCondition {

    record MinRatedGames(int games, Speed speed) implements SwissCondition, ArenaCondition {}
    record MaxRating(int rating, Speed speed)    implements SwissCondition, ArenaCondition {}
    record MinRating(int rating, Speed speed)    implements SwissCondition, ArenaCondition {}
    record Titled()                              implements SwissCondition, ArenaCondition {}
    record MinAccountAge(Period age)             implements SwissCondition, ArenaCondition {}
    record AllowList(List<String> users)         implements SwissCondition, ArenaCondition {
        public AllowList { users = users.stream().distinct().sorted().toList(); }
    }
    record AllowListHidden()                     implements SwissCondition, ArenaCondition {}
    record EntryCode()                           implements SwissCondition, ArenaCondition {}
    record Generic(String description)           implements SwissCondition, ArenaCondition {}

    record NotMissedSwiss()                      implements SwissCondition {}
    record Member(String teamId)                 implements ArenaCondition {}

    static Condition minRatedGames(int games, Speed speed) { return new MinRatedGames(games, speed); }
    static Condition minRating(int rating, Speed speed)    { return new MinRating(rating, speed); }
    static Condition maxRating(int rating, Speed speed)    { return new MaxRating(rating, speed); }
    static Condition titled()                              { return new Titled(); }
    static Condition minAccountAge(Period age)             { return new MinAccountAge(age); }
    static Condition allowListHidden()                     { return new AllowListHidden(); }
    static Condition allowList(List<String> users)         { return new AllowList(users); }
    static Condition entryCode()                           { return new EntryCode(); }
    static Condition notMissedSwiss()                      { return new NotMissedSwiss(); }
    static Condition member(String teamId)                 { return new Member(teamId); }
    static Condition memberByTeamName(String teamName)     { return member(teamName
                                                                        .replaceAll("-", "")
                                                                        .replaceAll(" ", "-")
                                                                        .replaceAll("--", "-")
                                                                        .replace("+", "")
                                                                        .replace("(", "")
                                                                        .replace(")", "")
                                                                        .replace(",", "")
                                                                        .replace(".", "")
                                                                        .replace("[", "")
                                                                        .replace("]", "")
                                                                        .toLowerCase(Locale.ROOT));
                                                           }
    static Condition generic(String description)           { return new Generic(description); }

    default String description() {
        return switch(this) {
            case MinRatedGames(int games, Speed speed) -> "≥ %d %s rated games".formatted(games, speed.name);
            case MaxRating(int rating, Speed speed)    -> "Rated ≤ %d in %s for the last week".formatted(rating, speed.name);
            case MinRating(int rating, Speed speed)    -> "Rated ≥ %d in %s".formatted(rating, speed.name);
            case Titled()                              -> "Only titled players";
            case MinAccountAge(Period age)             -> "%s old account".formatted(renderAge(age));
            case AllowList __                          -> "Fixed line-up";
            case AllowListHidden __                    -> "Fixed line-up";
            case EntryCode __                          -> "Needs entry code";
            case NotMissedSwiss __                     -> "Play your games";
            case Member(String teamId)                 -> "Must be in team %s".formatted(teamId); // Note, id vs name...
            case Generic(String description)           -> description;
        };
    }

    private String renderAge(Period age) {
        return switch(age) {
            case Period p when p.getDays()   == 1 -> "1 day";
            case Period p when p.getMonths() == 1 -> "1 month";
            case Period p when p.getYears()  == 1 -> "1 year";
            case Period p when p.getDays()    > 1 -> "%d days".formatted(p.getDays());
            case Period p when p.getMonths()  > 1 -> "%d months".formatted(p.getMonths());
            case Period p when p.getYears()   > 1 -> "%d years".formatted(p.getYears());
            default -> "%s".formatted(age.toString());
        };
    }
}
