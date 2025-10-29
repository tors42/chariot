FROM ghcr.io/lichess-org/lila-docker:main

RUN rm -rf /opt/java/openjdk

RUN curl -L -o jdk.tgz https://download.java.net/java/GA/jdk25.0.1/2fbf10d8c78e40bd87641c434705079d/8/GPL/openjdk-25.0.1_linux-$(uname -i)_bin.tar.gz
RUN tar xzf jdk.tgz
RUN mv jdk-25.0.1 /opt/java/openjdk

ADD FastAutoStartRelayAgent.java .

RUN javac -d agentclasses FastAutoStartRelayAgent.java

RUN printf 'Premain-Class: FastAutoStartRelayAgent \n\
Can-Retransform-Classes: true \n\
' >> agent.mf

RUN jar --create --file=/FastAutoStartRelayAgent.jar --manifest=agent.mf -C agentclasses .


ENV _JAVA_OPTIONS="--enable-preview -javaagent:/FastAutoStartRelayAgent.jar"

