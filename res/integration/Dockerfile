FROM ghcr.io/lichess-org/lila-docker:main

RUN rm -rf /opt/java/openjdk


RUN curl -L -o jdk.tgz https://download.java.net/java/GA/jdk27/55ce5470a6294008af0057ff4626d0e5/35/GPL/openjdk-27_linux-$(uname -i)_bin.tar.gz

RUN tar xzf jdk.tgz
RUN mv jdk-27* /opt/java/openjdk

ADD FastAutoStartRelayAgent.java .

RUN javac -d agentclasses FastAutoStartRelayAgent.java

RUN printf 'Premain-Class: FastAutoStartRelayAgent \n\
Can-Retransform-Classes: true \n\
' >> agent.mf

RUN jar --create --file=/FastAutoStartRelayAgent.jar --manifest=agent.mf -C agentclasses .


ENV _JAVA_OPTIONS="--enable-preview -javaagent:/FastAutoStartRelayAgent.jar"

