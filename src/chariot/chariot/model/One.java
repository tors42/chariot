package chariot.model;

import java.util.Optional;

/// A container for responses with a single entry.  
/// The response can be either {@link Some} for responses with a value or {@link Fail} for responses without a value.  
///
/// Here is an example where two requests are made and where one result has a value and the other doesn't,
/// {@snippet :
///      Client client = Client.basic();
///
///      One<Team> good = client.teams().byTeamId("lichess-swiss");
///      One<Team> bad  = client.teams().byTeamId("non-existing-team-id");
///
///      IO.println(good);  // Some[value=Team[id=lichess-swiss, name=...
///      IO.println(bad);   // Fail[status=404, message={"error":"Not found"}]
///      }
///
/// Typically one would pattern match on the result in order to process it,
/// {@snippet :
///      String process(One<Team> result) {
///          return switch(result) {
///              case Some(Team team)              -> "Found team! " + team.name();
///              case Fail(int status, String msg) -> "Couldn't find team! " + status;
///          };
///      }
///
///      String goodMessage = process(good);
///      String badMessage = process(bad);
///
///      IO.println(goodMessage); // Found team! Lichess Swiss
///      IO.println(badMessage);  // Couldn't find team! 404
///      }
///
/// Another way to process the result, is to use {@link #maybe()} to access it as an {@link Optional},
/// {@snippet :
///      client.teams().byTeamId("lichess-swiss").maybe()
///         .map(Team::name)
///         .ifPresent(name -> IO.println("Team: " + name)); // Team: Lichess Swiss
///      }
///
/// Or you could try to go yolo,
/// {@snippet :
///      Team team = client.teams().byTeamId("non-existing-team-id").get();
///
///      IO.println("Team name: " + team.name()) // NullPointerException!
///      }
public sealed interface One<T> permits
    Some,
    Fail {

    static <T> One<T> entry(T entry) {
        return new Some<>(entry);
    }

    static <T> One<T> fail(String message) {
        return fail(-1, message);
    }

    static <T> One<T> fail(int status, String message) {
        return new Fail<>(status, message);
    }

    Optional<T> maybe();
    T get();
}
