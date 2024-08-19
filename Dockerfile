FROM ubuntu:22.04 AS base
RUN apt-get update && \
    apt-get install -y npm && \
    npm install -g @bitwarden/cli && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Default to UTF-8 file.encoding
# From https://github.com/adoptium/containers/blob/d3c9617e83eb706aff74c095fd531fe31e359674/17/jre/ubuntu/jammy/Dockerfile.releases.full
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

FROM base AS test
ARG bitwardenEmailFolderId
ENV bitwardenEmailFolderId ${bitwardenEmailFolderId}
ARG BW_CLIENTID
ENV BW_CLIENTID ${BW_CLIENTID}
ARG BW_CLIENTSECRET
ENV BW_CLIENTSECRET ${BW_CLIENTSECRET}
ARG testItemId
ENV testItemId ${testItemId}
ARG testMasterPassword
ENV testMasterPassword ${testMasterPassword}
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src
ENV JAVA_HOME=/opt/java/openjdk
COPY --from=ibm-semeru-runtimes:open-21-jdk $JAVA_HOME $JAVA_HOME
ENV PATH="${JAVA_HOME}/bin:${PATH}"
RUN chmod +x mvnw && \
    ./mvnw --batch-mode test || ls target/surefire-reports/

FROM base AS production
EXPOSE 8080
ENV JAVA_HOME=/opt/java/openjdk
COPY --from=ibm-semeru-runtimes:open-21-jre $JAVA_HOME $JAVA_HOME
ENV PATH="${JAVA_HOME}/bin:${PATH}"
ADD /target/Email.jar Email.jar
ENTRYPOINT ["java","-jar","Email.jar"]
