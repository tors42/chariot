package build.snippets;

import java.util.List;

import chariot.ClientAuth;
import chariot.model.*;
import chariot.model.TimelineEntry.*;

class AccountAuth {

    static ClientAuth client = ClientAuth.basic().withToken(System.getenv("SNIPPET_TOKEN"));

    public static void main(String[] args) {
        while (true) {
            System.out.println("""

                Input number to run example.
                1) profile
                2) timeline

                Anything else to exit.
                """);

            switch(choose()) {
                case 1 -> profile();
                case 2 -> timeline();
                default -> System.exit(0);
            }
        }
    }

    static int choose() {
        var console = System.console();
        if (console == null) return -1;

        try {
            return Integer.parseInt(console.readLine());
        } catch(Exception e) {
            return -1;
        }
    }

    static void profile() {
        // @start region="profile"
        UserAuth result = client.account().profile().maybe().orElseThrow();
        String message = "Token owner %s %s".formatted(result.id(), result.patron() ? "is a patron" : "is not a patron");
        System.out.println(message); // @replace regex='.*' replacement=''
        // @end region="profile"
    }

    static void timeline() {
        // @start region="timeline"
        List<TimelineEntry> timelineTEMP1 = client.account().timeline().stream().toList(); //@replace substring='TEMP1' replacement=''
        //timeline.size() <= 15

        List<TimelineEntry> timeline = client.account().timeline(p -> p.nb(31)).stream().toList();
        //timeline.size() <= 30

        List<TimelineEntry> timelineTEMP2 = client.account().timeline(p -> p.nb(30).since(now -> now.minusHours(3))).stream().toList(); //@replace substring='TEMP2' replacement=''
        //timeline.size() == 2:ish

        for (var entry : timeline) {
            String message = switch(entry) {
                case Follow(var user, var otherUser, _)      -> "%s follows %s".formatted(user.name(), otherUser.name());
                case TeamJoin(var user, var teamId, _)       -> "%s joined team %s".formatted(user.name(), teamId);
                case TeamCreate(var user, var teamId, _)     -> "%s created team %s".formatted(user.name(), teamId);
                case ForumPost(var user, _, var name, _, _)  -> "%s posted in topic '%s'".formatted(user.name(), name);
                case UblogPost(var user, _, _, var name, _)  -> "%s posted blog '%s'".formatted(user.name(), name);
                case TourJoin(var user, _, var name, _)      -> "%s joined tournament '%s'".formatted(user.name(), name);
                case GameEnd(var fullId, _, _, _, _)         -> "Game %s ended".formatted(fullId);
                case SimulCreate(var user, _, var name, _)   -> "%s created simul '%s'".formatted(user.name(), name);
                case SimulJoin(var user, _, var name, _)     -> "%s joined simul '%s'".formatted(user.name(), name);
                case StudyLike(var user, _, var name, _)     -> "%s liked study '%s'".formatted(user.name(), name);
                case PlanStart(var user, _)                  -> "%s became patron".formatted(user.name());
                case PlanRenew(var user, int months, _)      -> "%s patron for %d months".formatted(user.name(), months);
                case BlogPost(_, _, var name, _)             -> "New blog %s".formatted(name);
                case UblogPostLike(var user, _, var name, _) -> "%s liked blog '%s'".formatted(user.name(), name);
                case StreamStart(var user, var name, _)      -> "%s started stream '%s'".formatted(user.name(), name);
            };
            System.out.println(message);
        }
        // @end region="timeline"
    }
}
