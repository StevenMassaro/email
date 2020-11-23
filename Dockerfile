FROM openjdk:13-alpine
EXPOSE 8080
ADD /target/Email.jar Email.jar
ENTRYPOINT ["java","-jar","Email.jar"]
