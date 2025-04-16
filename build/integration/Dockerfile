FROM ghcr.io/lichess-org/lila-docker:main

RUN rm -rf /opt/java/openjdk

RUN curl -L -o jdk.tgz https://download.java.net/java/GA/jdk24.0.1/24a58e0e276943138bf3e963e6291ac2/9/GPL/openjdk-24.0.1_linux-$(uname -i)_bin.tar.gz
RUN tar xzf jdk.tgz
RUN mv jdk-24.0.1 /opt/java/openjdk

ADD FastAutoStartRelayAgent.java .

RUN javac -d agentclasses FastAutoStartRelayAgent.java

RUN printf 'Premain-Class: FastAutoStartRelayAgent \n\
Can-Retransform-Classes: true \n\
' >> agent.mf

RUN jar --create --file=/FastAutoStartRelayAgent.jar --manifest=agent.mf -C agentclasses .


ENV _JAVA_OPTIONS="--enable-preview -javaagent:/FastAutoStartRelayAgent.jar"

