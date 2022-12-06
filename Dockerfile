FROM eclipse-temurin:17-jre as base
RUN apt-get update && apt-get install npm -y && \
    npm install -g @bitwarden/cli

FROM base as test
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src
RUN apt-get install openjdk-17-jdk -y && \
    chmod +x mvnw && \
    ./mvnw dependency:resolve && \
    ./mvnw test

FROM base as production
EXPOSE 8080
ADD /target/Email.jar Email.jar
ENTRYPOINT ["java","-jar","Email.jar"]
