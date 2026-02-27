FROM ghcr.io/lichess-org/lila-docker:main

RUN rm -rf /opt/java/openjdk

RUN curl -L -o jdk.tgz https://download.java.net/java/GA/jdk25.0.2/b1e0dfa218384cb9959bdcb897162d4e/10/GPL/openjdk-25.0.2_linux-$(uname -i)_bin.tar.gz
RUN tar xzf jdk.tgz
RUN mv jdk-25.0.2 /opt/java/openjdk

ADD FastAutoStartRelayAgent.java .

RUN javac -d agentclasses FastAutoStartRelayAgent.java

RUN printf 'Premain-Class: FastAutoStartRelayAgent \n\
Can-Retransform-Classes: true \n\
' >> agent.mf

RUN jar --create --file=/FastAutoStartRelayAgent.jar --manifest=agent.mf -C agentclasses .


ENV _JAVA_OPTIONS="--enable-preview -javaagent:/FastAutoStartRelayAgent.jar"

