# Chariot

Chariot is a Java client for the [Lichess API](https://lichess.org/api). It is compiled for Java 17.

Checkout the [JavaDoc](https://tors42.github.io/chariot/chariot/chariot/Client.html)

## Build Chariot

Make sure to use at least Java 18. A JDK archive can be downloaded and unpacked from https://jdk.java.net/

    $ java -version
    openjdk version "18.0.2.1" 2022-08-18
    OpenJDK Runtime Environment (build 18.0.2.1+1-1)
    OpenJDK 64-Bit Server VM (build 18.0.2.1+1-1, mixed mode, sharing)

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

## Use as dependency

The coordinates are `io.github.tors42:chariot:0.0.50`, so in a Maven project the following dependency can be added to the `pom.xml`:

```xml
    ...
    <dependency>
      <groupId>io.github.tors42</groupId>
      <artifactId>chariot</artifactId>
      <version>0.0.50</version>
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

[Lichess Rating Graph](https://github.com/TBestLittleHelper/SimpleGraphApplication) _Visualize rating (JavaFX)_  
[Lichess Search Engine Bot](https://github.com/jalpp/LichessSearchEngineBot) _Discord Bot for accessing Lichess features (Discord)_  
[Team Check](https://github.com/tors42/teamcheck) _Visualize team members (Swing)_  

[JBang Examples](https://github.com/tors42/jbang-chariot) _Various example scripts (JBang)_  
[Challenge AI Example](https://github.com/tors42/challengeaiexample) _OAuth2 PKCE example (Web Application)_ [Gitpod](https://gitpod.io/#https://github.com/tors42/challengeaiexample)  

# Contact

For any questions and/or feedback, feel free to open an issue in this project. Or use the [Discussions](https://github.com/tors42/chariot/discussions) feature of GitHub. Another means of contact could be through the [Lichess Discord server](https://discord.gg/lichess), look for the `#api-bots-boards` channel.

