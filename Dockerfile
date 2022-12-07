FROM ubuntu as base
RUN apt-get update && apt-get install npm -y && \
    npm install -g @bitwarden/cli

FROM base as build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src
RUN apt-get install openjdk-17-jdk -y && \
    chmod +x mvnw && \
    ./mvnw dependency:resolve && \
    ./mvnw --batch-mode --update-snapshots clean install --activate-profiles ui

FROM base as production
EXPOSE 8080
COPY --from=build /app/target/Email.jar Email.jar
RUN apt-get install openjdk-17-jre -y
ENTRYPOINT ["java","-jar","Email.jar"]
