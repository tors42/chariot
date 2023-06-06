# Chariot

Chariot is a Java client for the [Lichess API](https://lichess.org/api). It is compiled for Java 17.

## API Listing

<details>
<summary>Client.basic()</summary>

```java
import chariot.Client;

var client = Client.basic()


client.withToken()
client.withPkce()

client.analysis().cloudEval()

client.bot().botsOnline()

client.broadcasts().official()
client.broadcasts().exportPgn()
client.broadcasts().exportOneRoundPgn()
client.broadcasts().streamBroadcast()

client.challenges().challengeOpenEnded()

client.externalEngine().acquire()
client.externalEngine().answer()
client.externalEngine().analyse()

client.games().byGameId()
client.games().byGameIds()
client.games().byUserId()
client.games().currentByUserId()
client.games().moveInfosByGameId()
client.games().gameInfosByUserIds()
client.games().addGameIdsToStream()
client.games().tvFeed()
client.games().tvChannels()
client.games().byChannel()
client.games().importGame()

client.openingExplorer().masters()
client.openingExplorer().lichess()
client.openingExplorer().player()

client.puzzles().dailyPuzzle()
client.puzzles().byId()
client.puzzles().stormDashboard()

client.simuls().simuls()

client.studies().exportStudiesByUserId()
client.studies().exportChaptersByStudyId()
client.studies().exportChapterByStudyAndChapterId()
client.studies().lastModifiedByStudyId()
client.studies().listStudiesByUser()

client.tablebase().standard()
client.tablebase().atomic()
client.tablebase().antichess()

client.teams().numberOfTeams()
client.teams().search()
client.teams().searchByPage()
client.teams().popularTeams()
client.teams().byTeamId()
client.teams().byUserId()
client.teams().usersByTeamId()
client.teams().arenaByTeamId()
client.teams().swissByTeamId()

client.tournaments().currentTournaments()
client.tournaments().arenasCreatedByUserId()
client.tournaments().arenaById()
client.tournaments().swissById()
client.tournaments().gamesByArenaId()
client.tournaments().gamesBySwissId()
client.tournaments().resultsByArenaId()
client.tournaments().resultsBySwissId()
client.tournaments().swissTRF()
client.tournaments().teamBattleResultsById()

client.users().byId()
client.users().byIds()
client.users().top10()
client.users().leaderboard()
client.users().liveStreamers()
client.users().activityById()
client.users().statusByIds()
client.users().ratingHistoryById()
client.users().crosstable()
client.users().performanceStatisticsByIdAndType()
client.users().autocompleteNames()
client.users().autocompleteUsers()
```
</details>

<details>
<summary>Client.auth()</summary>

```java
import chariot.Client;

var client = Client.auth()

client.revokeToken()

client.account().profile()
client.account().following()
client.account().emailAddress()
client.account().preferences()
client.account().kidMode()

client.board().connect()
client.board().challenge()
client.board().challengeKeepAlive()
client.board().challengeAI()
client.board().acceptChallenge()
client.board().declineChallenge()
client.board().cancelChallenge()
client.board().seekCorrespondence()
client.board().seekRealTime()
client.board().connectToGame()
client.board().move()
client.board().abort()
client.board().resign()
client.board().handleDrawOffer()
client.board().handleTakebackOffer()
client.board().claimVictory()
client.board().berserk()
client.board().chat()
client.board().chatSpectators()
client.board().fetchChat()

client.bot().upgradeToBotAccount()
client.bot().connect()
client.bot().challenge()
client.bot().challengeKeepAlive()
client.bot().challengeAI()
client.bot().acceptChallenge()
client.bot().declineChallenge()
client.bot().cancelChallenge()
client.bot().connectToGame()
client.bot().move()
client.bot().abort()
client.bot().resign()
client.bot().chat()
client.bot().chatSpectators()
client.bot().fetchChat()

client.broadcasts().create()
client.broadcasts().update()
client.broadcasts().createRound()
client.broadcasts().updateRound()
client.broadcasts().broadcastById()
client.broadcasts().roundById()
client.broadcasts().pushPgnByRoundId()

client.challenges().connect()
client.challenges().challenges()
client.challenges().challenge()
client.challenges().challengeKeepAlive()
client.challenges().challengeAI()
client.challenges().acceptChallenge()
client.challenges().declineChallenge()
client.challenges().cancelChallenge()
client.challenges().addTimeToGame()
client.challenges().bulks()
client.challenges().createBulk()
client.challenges().startBulk()
client.challenges().cancelBulk()
client.challenges().startClocksOfGame()

client.externalEngine().list()
client.externalEngine().get()
client.externalEngine().create()
client.externalEngine().update()
client.externalEngine().delete()

client.games().ongoing()

client.puzzles().activity()
client.puzzles().createAndJoinRace()
client.puzzles().puzzleDashboard()

client.teams().joinTeam()
client.teams().leaveTeam()
client.teams().messageTeam()
client.teams().kickFromTeam()
client.teams().requests()
client.teams().requestsDeclined()
client.teams().requestAccept()
client.teams().requestDecline()

client.tournaments().joinArena()
client.tournaments().joinSwiss()
client.tournaments().withdrawArena()
client.tournaments().withdrawSwiss()
client.tournaments().createArena()
client.tournaments().createSwiss()
client.tournaments().updateArena()
client.tournaments().updateSwiss()
client.tournaments().scheduleNextRoundSwiss()
client.tournaments().terminateArena()
client.tournaments().terminateSwiss()
client.tournaments().generateUserEntryCodes()
client.tournaments().updateTeamBattle()

client.users().sendMessageToUser()
client.users().followUser()
client.users().unfollowUser()
```
</details>

Checkout the [JavaDoc](https://tors42.github.io/chariot/chariot/chariot/Client.html) for details.

## Use as Dependency

The coordinates are `io.github.tors42:chariot:0.0.67`, so in a Maven project
the following dependency can be added to the `pom.xml`:

```xml
    ...
    <dependency>
      <groupId>io.github.tors42</groupId>
      <artifactId>chariot</artifactId>
      <version>0.0.67</version>
    </dependency>
    ...
```

Here is a link to a simple example Maven project application
https://github.com/tors42/chariot-example which can be imported into an IDE in
order to get things like code completion support and other good stuff.


## Build Chariot

Build with latest Java. A JDK archive can be downloaded and unpacked from https://jdk.java.net/

    $ java -version
    openjdk version "20.0.1" 2023-04-18
    OpenJDK Runtime Environment (build 20.0.1+9-29)
    OpenJDK 64-Bit Server VM (build 20.0.1+9-29, mixed mode, sharing)

    $ java build/Build.java
    55 successful tests
    0 failed tests

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

An example which feeds moves to a Board in order to track FEN updates, and "draws" the board with text.

```java
package build;

import java.util.List;

import chariot.util.Board;

class FEN {
    public static void main(String[] args) {

        Board initialBoard = Board.fromStandardPosition();

        List<String> validMovesUCI = initialBoard.validMoves().stream()
            .map(Board.Move::uci)
            .sorted()
            .toList();

        List<String> validMovesSAN = validMovesUCI.stream()
            .map(initialBoard::toSAN)
            .toList();

        String movesToPlay = "e4 e5 Nf3 Nc6"; // (UCI also ok, "e2e4 e7e5 g1f3 b8c6")

        Board resultingBoard = initialBoard.play(movesToPlay);

        System.out.println(String.join("\n",
                "Initial FEN: "         + initialBoard.toFEN(),
                "Initial Board:\n"      + initialBoard,
                "Valid moves (UCI): "   + validMovesUCI,
                "Valid moves (SAN): "   + validMovesSAN.stream().map("%4s"::formatted).toList(),
                "Playing: "             + movesToPlay,
                "Resulting FEN: "       + resultingBoard.toFEN(),
                "Resulting Board:\n"    + resultingBoard,
                "Board (letter, frame, coordinates):\n" +
                resultingBoard.toString(c -> c.letter().frame().coordinates())
                ));
    }

   public static long costOfThisProgramBecomingSkyNet() {
        return Long.MAX_VALUE; // https://xkcd.com/534/
   }
}
```

    $ java -p out/modules --add-modules chariot build/FEN.java
    Initial FEN: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
    Initial Board:
    ♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜
    ♟ ♟ ♟ ♟ ♟ ♟ ♟ ♟
                   
                   
                   
                   
    ♙ ♙ ♙ ♙ ♙ ♙ ♙ ♙
    ♖ ♘ ♗ ♕ ♔ ♗ ♘ ♖
    Valid moves (UCI): [a2a3, a2a4, b1a3, b1c3, b2b3, b2b4, c2c3, c2c4, d2d3, d2d4, e2e3, e2e4, f2f3, f2f4, g1f3, g1h3, g2g3, g2g4, h2h3, h2h4]
    Valid moves (SAN): [  a3,   a4,  Na3,  Nc3,   b3,   b4,   c3,   c4,   d3,   d4,   e3,   e4,   f3,   f4,  Nf3,  Nh3,   g3,   g4,   h3,   h4]
    Playing: e4 e5 Nf3 Nc6
    Resulting FEN: r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 3
    Resulting Board:
    ♜   ♝ ♛ ♚ ♝ ♞ ♜
    ♟ ♟ ♟ ♟   ♟ ♟ ♟
        ♞          
            ♟      
            ♙      
              ♘    
    ♙ ♙ ♙ ♙   ♙ ♙ ♙
    ♖ ♘ ♗ ♕ ♔ ♗   ♖
    Board (letter, frame, coordinates):
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

# Applications

A list of applications using Chariot,

[Bobby](https://github.com/teemoo7/bobby) _Chess Engine with Lichess Bot integration (Java)_  
[Lichess Rating Graph](https://github.com/TBestLittleHelper/SimpleGraphApplication) _Visualize rating (JavaFX)_  
[Lichess Search Engine Bot](https://github.com/jalpp/LichessSearchEngineBot) _Discord Bot for accessing Lichess features (Discord)_  
[Team Check](https://github.com/tors42/teamcheck) _Visualize team members (Swing)_  
[charibot](https://github.com/tors42/charibot) _Application to control a Lichess BOT account (makes random moves) (Java) [@charibot](https://lichess.org/@/charibot)_  
[jc](https://github.com/tors42/jc) _Watch the featured LichessTV game in terminal or Play a casual Rapid game with the Board API (Swing/Text)_  
[ee](https://github.com/tors42/ee) _External Engine - Use local desktop chess engine in Lichess Analysis web browser (Swing)_  

[JBang Examples](https://github.com/tors42/jbang-chariot) _Various example scripts (JBang)_  
[Challenge AI Example](https://github.com/tors42/challengeaiexample) _OAuth2 PKCE example (Web Application)_  
[Playground](https://github.com/tors42/playground) _GitHub Codespaces repo to write code in browser_  
[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://github.com/codespaces/new?hide_repo_select=true&ref=main&repo=586354374)  

# Contact

For any questions and/or feedback, feel free to open an issue in this project. Or use the [Discussions](https://github.com/tors42/chariot/discussions) feature of GitHub. Another means of contact could be through the [Lichess Discord server](https://discord.gg/lichess), look for the `#lichess-api-support` channel.

