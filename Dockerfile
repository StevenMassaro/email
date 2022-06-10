FROM openjdk:13
EXPOSE 8080
RUN yum install unzip wget -y && \
    wget --no-verbose https://github.com/bitwarden/cli/releases/download/v1.20.0/bw-linux-1.20.0.zip && \
    unzip bw-linux-1.20.0.zip && \
    install bw /usr/local/bin/ && \
    rm -rf bw*
ADD /target/Email.jar Email.jar
ENTRYPOINT ["java","-jar","Email.jar"]
