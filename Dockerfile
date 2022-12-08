FROM eclipse-temurin:17-jdk as base
RUN apt-get update && apt-get install npm -y && \
    npm install -g @bitwarden/cli@2022.10.0

FROM base as test
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src
RUN chmod +x mvnw && \
    ./mvnw --batch-mode test

FROM base as production
EXPOSE 8080
ADD /target/Email.jar Email.jar
ENTRYPOINT ["java","-jar","Email.jar"]
