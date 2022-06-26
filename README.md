# Chariot

Chariot is a Java client for the [Lichess API](https://lichess.org/api). It is compiled for Java 17.

Checkout the [JavaDoc](https://tors42.github.io/chariot/chariot/chariot/Client.html)

## Run Example using JBang

Below are a couple of example scripts which makes use of Chariot.

### basic.jsh

The first one shows basic usage of fetching a team and showing its current member count.

```java
//DEPS io.github.tors42:chariot:0.0.38
//JAVA 17+
import chariot.Client;

var client = Client.basic();
var team = client.teams().byTeamId("lichess-swiss").get();
System.out.printf("Team %s has %d members!%n", team.name(), team.nbMembers());
```

The script can be run by using a tool called JBang, which has support to download both Java and the Chariot client.
JBang can be found at https://www.jbang.dev/download/

    $ jbang basic.jsh
    Team Lichess Swiss has 201672 members!

### tournament.jsh

The second script makes use of an operation which needs authorization - it creates a Swiss tournament.

```java
//DEPS io.github.tors42:chariot:0.0.38
//JAVA 17+
import chariot.Client;
import java.time.*;

var client = Client.reuseOrInitialize(params -> params
    .prefs("my-preferences")
    .scopes(Client.Scope.tournament_write));

// Could also have used 'var client = Client.auth("my-token");'

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
```

The first run the script will output a Lichess URL where it is possible to grant access for the needed `tournament:write` scope.
Consecutive runs the script will run without need for interaction:

    $ jbang tournament.jsh
    One[entry=Swiss[id=vLx22Ff1, name=My 5+3 Swiss, createdBy=test, startsAt=2022-03-29T17:00:00.000+02:00, status=created, nbOngoing=0, nbPlayers=0, nbRounds=9, round=0, rated=false, variant=standard, clock=Clock[limit=300, increment=3], greatPlayer=null, nextRound=NextRound[at=2022-03-29T17:00:00.000+02:00, in=62693], quote=null]]

## Use as dependency

The coordinates are `io.github.tors42:chariot:0.0.38`, so in a Maven project the following dependency can be added to the `pom.xml`:

    ...
    <dependency>
      <groupId>io.github.tors42</groupId>
      <artifactId>chariot</artifactId>
      <version>0.0.38</version>
    </dependency>
    ...

Here is a link to a simple example Maven project application https://github.com/tors42/chariot-example which can be imported into an IDE in order to get things like code completion support and other good stuff.

## Build Chariot

Make sure to use at least Java 18. A JDK archive can be downloaded and unpacked from https://jdk.java.net/18/

    $ java -version
    openjdk version "18.0.1.1" 2022-04-22
    OpenJDK Runtime Environment (build 18.0.1.1+2-6)
    OpenJDK 64-Bit Server VM (build 18.0.1.1+2-6, mixed mode, sharing)

    $ java build/Build.java

The resulting artifact, `out/modules/chariot-0.0.1-SNAPSHOT.jar`, will be compatible with Java release 17

### Serve JavaDoc

A simple way to check the generated JavaDoc is by serving it locally using the Java 18 included Simple Web Server tool `jwebserver`,

    $ cd out/javadoc/
    $ jwebserver
    Binding to loopback by default. For all interfaces use "-b 0.0.0.0" or "-b ::".
    Serving /tmp/chariot/out/javadoc and subdirectories on 127.0.0.1 port 8000
    URL http://127.0.0.1:8000/

And then visting http://127.0.0.1:8000/ with a Web Browser

### Explore Chariot (without writing an application)

The JDK includes a tool called JShell. It can be used to run Java code and is suitable for exploring Java libraries.

    $ jshell --module-path out/modules --add-module chariot
    |  Welcome to JShell -- Version 18.0.1.1
    |  For an introduction type: /help intro
    jshell> var client = chariot.Client.basic()
    client ==> chariot.internal.BasicClient@1184ab05
    
    jshell> client.users().statusByIds("lichess").ifPresent(u -> System.out.println("Online: " + u.online()))
    Online: true
    
    jshell> /exit
    | Goodbye


### Run Example (non-project, single-file application)

    $ java --module-path out/modules --add-modules chariot build/Example.java
    Lichess Swiss has 217049 members!

# Applications

A (short) list of notable applications using Chariot,

[Lichess Rating Graph](https://github.com/TBestLittleHelper/SimpleGraphApplication) _Visualize rating (JavaFX)_  
[Lichess Search Engine Bot](https://github.com/jalpp/LichessSearchEngineBot) _Discord Bot for accessing Lichess features (Discord)_  
[Team Check](https://github.com/tors42/teamcheck) _Visualize team members (Swing)_  

[JBang Examples](https://github.com/tors42/jbang-chariot) _Various example scripts (JBang)_  
[Challenge AI Example](https://github.com/tors42/challengeaiexample) _OAuth2 PKCE example (Web Application)_ [Heroku](https://challengeaiexample.herokuapp.com)  

# Contact

For any questions and/or feedback, feel free to open an issue in this project. Or use the [Discussions](https://github.com/tors42/chariot/discussions) feature of GitHub. Another means of contact could be through the [Lichess Discord server](https://discord.gg/lichess), look for the `#api-bots-boards` channel.

