import chariot.Client;
import java.util.Arrays;

class Example {

    static String defaultTeamId = "lichess-swiss"; // https://lichess.org/team/lichess-swiss

    public static void main(String... args) {
        String teamId = Arrays.stream(args).findFirst().orElse(defaultTeamId);

        var client = Client.basic();
        var result = client.teams().byTeamId(teamId);

        if (result.isPresent()) {
            var team = result.get();
            System.out.println(team.name() + " has " + team.nbMembers() + " members!");
        } else {
            System.out.format("""
                    Couldn't find the team with team id "%s"
                    Note, a team id should be all lowercase and instead of whitespace there are dashes.
                    The team "Lichess Swiss" for instance, has the team id "lichess-swiss".
                    """, teamId);
        }
    }
}
