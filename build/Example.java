//DEPS io.github.tors42:chariot:0.0.42
//JAVA 17+
import chariot.Client;
import java.util.*;

class Example {

    // https://lichess.org/team/lichess-swiss
    static String defaultTeamId = "lichess-swiss";

    public static void main(String... args) {
        String teamId = Arrays.stream(args).findFirst().orElse(defaultTeamId);

        var client = Client.basic();

        client.teams().byTeamId(teamId).ifPresentOrElse(
                team -> System.out.format("%s has %d members%n", team.name(), team.nbMembers()),
                () -> System.out.format("""
                    Couldn't find the team with team id "%s"
                    Note, a team id should be all lowercase and instead of
                    whitespace there are dashes. The team "Lichess Swiss" for
                    instance, has the team id "lichess-swiss".
                    """, teamId));

        List<String> members = client.teams().usersByTeamId(teamId).stream()
            .limit(3)
            .map(user -> user.username())
            .toList();

        if (! members.isEmpty()) {
            System.out.println("\nSome members in the team:");
            members.forEach(System.out::println);
        }
    }
}
