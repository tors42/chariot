//DEPS io.github.tors42:chariot:0.0.46
//JAVA 17+
import chariot.Client;
import java.util.*;

class Example {

    // https://lichess.org/team/lichess-swiss
    static String defaultTeamId = "lichess-swiss";

    public static void main(String... args) {
        String teamId = Arrays.stream(args).findFirst().orElse(defaultTeamId);

        var client = Client.basic();

        String message = client.teams().byTeamId(teamId)
            .map(team -> "%s has %d members".formatted(team.name(), team.nbMembers()))
            .orElse("""
                Couldn't find the team with team id "%s"
                Note, a team id should be all lowercase and instead of
                whitespace there are dashes. The team "Lichess Swiss" for
                instance, has the team id "lichess-swiss".
                """.formatted(teamId));

        System.out.println(message);

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
