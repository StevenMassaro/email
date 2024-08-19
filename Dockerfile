FROM alpine:3.18 AS base
RUN apk add --no-cache --update npm && \
    npm install -g @bitwarden/cli

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
RUN apk add --no-cache openjdk17-jdk
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src
RUN chmod +x mvnw && \
    ./mvnw --batch-mode test

FROM base AS production
EXPOSE 8080
RUN apk add --no-cache openjdk17-jre-headless
ADD /target/Email.jar Email.jar
ENTRYPOINT ["java","-jar","Email.jar"]
