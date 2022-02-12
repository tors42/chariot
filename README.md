# Chariot

Java client for the [Lichess API](https://lichess.org/api)

Checkout the [JavaDoc](https://tors42.github.io/chariot/chariot/chariot/Client.html)


## Build

Make sure to use at least Java 17. A JDK archive can be downloaded and unpacked from https://jdk.java.net/17/

    $ export JAVA_HOME=<path to jdk>
    $ export PATH=$JAVA_HOME/bin:$PATH

    $ java -version
    openjdk version "17.0.2" 2022-01-18
    OpenJDK Runtime Environment (build 17.0.2+8-86)
    OpenJDK 64-Bit Server VM (build 17.0.2+8-86, mixed mode, sharing)

    $ java build/Build.java

## Explore Chariot (without writing an application)

The JDK includes a tool called JShell. It can be used to run Java code and is suitable for exploring Java libraries.

    $ jshell --module-path out/modules --add-module chariot
    |  Welcome to JShell -- Version 17.0.2
    |  For an introduction type: /help intro
    jshell> var client = chariot.Client.basic()
    client ==> chariot.internal.BasicClient@1184ab05
    
    jshell> client.users().statusByIds("lichess").ifPresent(u -> System.out.println("Online: " + u.online()))
    Online: true
    
    jshell> /exit
    | Goodbye


## Run Example (non-project, single-file application)

    $ java --module-path out/modules --add-modules chariot build/Example.java

## Use as dependency

The coordinates are `io.github.tors42:chariot:0.0.24`, so in a Maven project the following dependency can be added to the `pom.xml`:

    ...
    <dependency>
      <groupId>io.github.tors42</groupId>
      <artifactId>chariot</artifactId>
      <version>0.0.24</version>
    </dependency>
    ...

Here's a mini [example Maven application](https://github.com/tors42/chariot-example)

# Applications

A list of notable applications using Chariot,

[Team Check](https://github.com/tors42/teamcheck) _Visualize team members_


