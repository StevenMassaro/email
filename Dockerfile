FROM eclipse-temurin:17-jre
EXPOSE 8080
RUN apt-get update && apt-get install npm -y && \
    npm install -g @bitwarden/cli@2022.10.0 && \
    bw --version
ADD /target/Email.jar Email.jar
ENTRYPOINT ["java","-jar","Email.jar"]
