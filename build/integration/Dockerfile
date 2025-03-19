FROM ghcr.io/lichess-org/lila-docker:main

RUN rm -rf /opt/java/openjdk

RUN curl -L -o jdk.tgz https://download.java.net/java/GA/jdk24/1f9ff9062db4449d8ca828c504ffae90/36/GPL/openjdk-24_linux-$(uname -i)_bin.tar.gz
RUN tar xzf jdk.tgz
RUN mv jdk-24 /opt/java/openjdk

ADD FastAutoStartRelayAgent.java .

RUN javac --enable-preview --release 24 -d agentclasses FastAutoStartRelayAgent.java

RUN printf 'Premain-Class: FastAutoStartRelayAgent \n\
Can-Retransform-Classes: true \n\
' >> agent.mf

RUN jar --create --file=/FastAutoStartRelayAgent.jar --manifest=agent.mf -C agentclasses .


ENV _JAVA_OPTIONS="--enable-preview -javaagent:/FastAutoStartRelayAgent.jar"

