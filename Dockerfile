FROM ubuntu as base
RUN apt-get update && apt-get install npm -y && \
    npm install -g @bitwarden/cli@2022.10.0

FROM base as build
WORKDIR /app
RUN apt-get install openjdk-17-jdk -y
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src
RUN chmod +x mvnw && \
    ./mvnw --batch-mode --update-snapshots clean install --activate-profiles ui

FROM base as production
EXPOSE 8080
RUN apt-get install openjdk-17-jre -y
COPY --from=build /app/target/Email.jar Email.jar
ENTRYPOINT ["java","-jar","Email.jar"]
