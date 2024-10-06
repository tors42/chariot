FROM ghcr.io/lichess-org/lila-docker:main

RUN rm -rf /opt/java/openjdk

RUN curl -L -o jdk.tgz https://download.java.net/java/GA/jdk23/3c5b90190c68498b986a97f276efd28a/37/GPL/openjdk-23_linux-$(uname -i)_bin.tar.gz
RUN tar xzf jdk.tgz
RUN mv jdk-23 /opt/java/openjdk

ADD FastAutoStartRelayAgent.java .

RUN javac --enable-preview --release 23 -d agentclasses FastAutoStartRelayAgent.java

RUN printf 'Premain-Class: FastAutoStartRelayAgent \n\
Can-Retransform-Classes: true \n\
' >> agent.mf

RUN jar --create --file=/FastAutoStartRelayAgent.jar --manifest=agent.mf -C agentclasses .


ENV _JAVA_OPTIONS="--enable-preview -javaagent:/FastAutoStartRelayAgent.jar"

