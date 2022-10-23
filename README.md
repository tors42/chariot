# Chariot

Chariot is a Java client for the [Lichess API](https://lichess.org/api). It is compiled for Java 17.

Checkout the [JavaDoc](https://tors42.github.io/chariot/chariot/chariot/Client.html)

## Build Chariot

Make sure to use at least Java 19. A JDK archive can be downloaded and unpacked from https://jdk.java.net/

    $ java -version
    openjdk version "19.0.1" 2022-10-18
    OpenJDK Runtime Environment (build 19.0.1+10-21)
    OpenJDK 64-Bit Server VM (build 19.0.1+10-21, mixed mode, sharing)

    $ java build/Build.java

The resulting artifact, `out/modules/chariot-0.0.1-SNAPSHOT.jar`, will be compatible with Java release 17

## Examples (non-project, single files)

### 1. example.jsh

An example of fetching a team and showing its current member count - using JShell

```java
import chariot.Client;

var client = Client.basic();
System.out.println(client.teams().byTeamId("lichess-swiss")
    .map(team -> "Team %s has %d members!".formatted(team.name(), team.nbMembers()))
    .orElse("Couldn't find team!"));
```

    $ jshell --module-path out/modules --add-module chariot -q build/example.jsh
    Team Lichess Swiss has 250795 members!
    jshell>

Tip, it is possible to write code interactively in JShell

    jshell> client.teams().numberOfTeams();
    $4 ==> 284025
    jshell> /exit

### 2. Example.java

An example which uses a token to authenticate in order to be able to create a Swiss tournament

```java
package build;

import chariot.Client;
import java.time.*;

class Example {

    public static void main(String[] args) {

        var client = Client.auth("my-token");

        var tomorrow = ZonedDateTime.now().plusDays(1).with(
            LocalTime.parse("17:00"));

        String teamId = "my-team-id";

        var result = client.tournaments().createSwiss(teamId, params -> params
            .clockBlitz5m3s()
            .name("My 5+3 Swiss")
            .rated(false)
            .description("Created via API")
            .startsAt(tomorrow));

        System.out.println(result);
    }
}
```

    $ java -p out/modules --add-modules chariot build/Example.java
    Entry[entry=Swiss[id=vLx22Ff1, name=My 5+3 Swiss, createdBy=test, startsAt=2022-03-29T17:00:00.000+02:00, status=created, nbOngoing=0, nbPlayers=0, nbRounds=9, round=0, rated=false, variant=standard, clock=Clock[limit=300, increment=3], greatPlayer=null, nextRound=NextRound[at=2022-03-29T17:00:00.000+02:00, in=62693], quote=null]]

### 3. FEN.java

An example which parses PGN data and feeds moves to a Board in order to track FEN updates, and "draws" the board with text.

```java
package build;

import java.util.List;

import chariot.model.Pgn;
import chariot.util.Board;

class FEN {
    public static void main(String[] args) {

        List<Pgn> pgnList = Pgn.readFromString("""
            [Event "Testing"]

            1. e4 e5 2. Nf3 Nc6
            """);

        List<String> moves = pgnList.get(0).moveListSAN();

        Board board = Board.fromStandardPosition();

        String initialFEN = board.toFEN();

        for (String move : moves) {
            board = board.play(move);
        }

        System.out.println("Initial: " + initialFEN);
        System.out.println(Board.fromFEN(initialFEN));
        System.out.println("Valid moves#: " + Board.fromFEN(initialFEN).validMoves().size());
        System.out.println("Play: " + moves.toString());
        System.out.println(board.toFEN());
        System.out.println(board.toString());
        System.out.println(board.toString(c -> c.letter().frame().coordinates()));
   }

   public static long costOfThisProgramBecomingSkyNet() {
        return Long.MAX_VALUE; // https://xkcd.com/534/
   }
}
```

    $ java -p out/modules --add-modules chariot build/FEN.java
    Initial: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
    ♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜
    ♟ ♟ ♟ ♟ ♟ ♟ ♟ ♟
    
    
    
    
    ♙ ♙ ♙ ♙ ♙ ♙ ♙ ♙
    ♖ ♘ ♗ ♕ ♔ ♗ ♘ ♖
    Valid moves#: 20
    Play: [e4, e5, Nf3, Nc6]
    r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 3
    ♜   ♝ ♛ ♚ ♝ ♞ ♜
    ♟ ♟ ♟ ♟   ♟ ♟ ♟
        ♞
            ♟
            ♙
              ♘
    ♙ ♙ ♙ ♙   ♙ ♙ ♙
    ♖ ♘ ♗ ♕ ♔ ♗   ♖
      ┌───┬───┬───┬───┬───┬───┬───┬───┐
    8 │ r │   │ b │ q │ k │ b │ n │ r │
      ├───┼───┼───┼───┼───┼───┼───┼───┤
    7 │ p │ p │ p │ p │   │ p │ p │ p │
      ├───┼───┼───┼───┼───┼───┼───┼───┤
    6 │   │   │ n │   │   │   │   │   │
      ├───┼───┼───┼───┼───┼───┼───┼───┤
    5 │   │   │   │   │ p │   │   │   │
      ├───┼───┼───┼───┼───┼───┼───┼───┤
    4 │   │   │   │   │ P │   │   │   │
      ├───┼───┼───┼───┼───┼───┼───┼───┤
    3 │   │   │   │   │   │ N │   │   │
      ├───┼───┼───┼───┼───┼───┼───┼───┤
    2 │ P │ P │ P │ P │   │ P │ P │ P │
      ├───┼───┼───┼───┼───┼───┼───┼───┤
    1 │ R │ N │ B │ Q │ K │ B │   │ R │
      └───┴───┴───┴───┴───┴───┴───┴───┘
        a   b   c   d   e   f   g   h

## Use as dependency

The coordinates are `io.github.tors42:chariot:0.0.54`, so in a Maven project the following dependency can be added to the `pom.xml`:

```xml
    ...
    <dependency>
      <groupId>io.github.tors42</groupId>
      <artifactId>chariot</artifactId>
      <version>0.0.54</version>
    </dependency>
    ...
```

Here is a link to a simple example Maven project application https://github.com/tors42/chariot-example which can be imported into an IDE in order to get things like code completion support and other good stuff.

### Serve JavaDoc

A simple way to check the generated JavaDoc is by serving it locally using the Java 18 included Simple Web Server tool `jwebserver`,

    $ cd out/javadoc/
    $ jwebserver
    Binding to loopback by default. For all interfaces use "-b 0.0.0.0" or "-b ::".
    Serving /tmp/chariot/out/javadoc and subdirectories on 127.0.0.1 port 8000
    URL http://127.0.0.1:8000/

And then visting http://127.0.0.1:8000/ with a Web Browser

# Applications

A (short) list of notable applications using Chariot,

[Bobby](https://github.com/teemoo7/bobby) _Chess Engine with Lichess Bot integration (Java)_  
[Lichess Rating Graph](https://github.com/TBestLittleHelper/SimpleGraphApplication) _Visualize rating (JavaFX)_  
[Lichess Search Engine Bot](https://github.com/jalpp/LichessSearchEngineBot) _Discord Bot for accessing Lichess features (Discord)_  
[Team Check](https://github.com/tors42/teamcheck) _Visualize team members (Swing)_  
[JC](https://github.com/tors42/jc) _Currently shows the featured LichessTV game in console (Terminal)_  

[JBang Examples](https://github.com/tors42/jbang-chariot) _Various example scripts (JBang)_  
[Challenge AI Example](https://github.com/tors42/challengeaiexample) _OAuth2 PKCE example (Web Application)_ [Gitpod](https://gitpod.io/#https://github.com/tors42/challengeaiexample)  

# Contact

For any questions and/or feedback, feel free to open an issue in this project. Or use the [Discussions](https://github.com/tors42/chariot/discussions) feature of GitHub. Another means of contact could be through the [Lichess Discord server](https://discord.gg/lichess), look for the `#lichess-api-support` channel.

