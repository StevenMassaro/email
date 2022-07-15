FROM openjdk:17
EXPOSE 8080
RUN microdnf install unzip wget -y && \
    wget --no-verbose https://github.com/bitwarden/cli/releases/download/v1.22.1/bw-linux-1.22.1.zip && \
    unzip bw-linux-1.22.1.zip && \
    install bw /usr/local/bin/ && \
    rm -rf bw* && \
    microdnf remove unzip wget -y
ADD /target/Email.jar Email.jar
ENTRYPOINT ["java","-jar","Email.jar"]
