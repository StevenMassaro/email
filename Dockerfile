FROM eclipse-temurin:17-jre as base
RUN apt-get update && apt-get install npm -y && \
    npm install -g @bitwarden/cli

FROM base as test-base
RUN apt-get install openjdk-17-jdk -y
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:resolve
COPY src ./src

FROM test-base as test
RUN ["./mvnw", "test"]

FROM base as production
EXPOSE 8080
ADD /target/Email.jar Email.jar
ENTRYPOINT ["java","-jar","Email.jar"]
