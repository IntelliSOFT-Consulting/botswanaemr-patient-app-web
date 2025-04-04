FROM maven:3.6.3-jdk-11-slim AS build
FROM openjdk:11
EXPOSE 8081
WORKDIR /botswana_emr_auth_javabackend
COPY . /botswana_emr_auth_javabackend
ADD target/BotswanaEMRAuthentication-0.0.1-SNAPSHOT.jar BotswanaEMRAuthentication-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","target/BotswanaEMRAuthentication-0.0.1-SNAPSHOT.jar"]