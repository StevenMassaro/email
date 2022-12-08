FROM ubuntu as base
RUN apt-get update && \
    apt-get install npm locales --yes --no-install-recommends && \
    npm install -g @bitwarden/cli@2022.10.0

# Default to UTF-8 file.encoding
# From https://github.com/adoptium/containers/blob/d3c9617e83eb706aff74c095fd531fe31e359674/17/jre/ubuntu/jammy/Dockerfile.releases.full
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen \
    && locale-gen en_US.UTF-8

FROM base as test
WORKDIR /app
RUN apt-get install openjdk-17-jdk -y
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src
RUN chmod +x mvnw && \
    ./mvnw --batch-mode test

FROM base as production
EXPOSE 8080
RUN apt-get install openjdk-17-jre -y
ADD /target/Email.jar Email.jar
ENTRYPOINT ["java","-jar","Email.jar"]
