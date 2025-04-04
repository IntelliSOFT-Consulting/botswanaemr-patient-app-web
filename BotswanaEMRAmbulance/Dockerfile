FROM maven:3.6.3-jdk-11-slim AS build
FROM openjdk:11
EXPOSE 7002
WORKDIR /botswana_emr_ambulance_java_backend
COPY . /botswana_emr_ambulance_java_backend
ADD target/BotswanaEMRAmbulance.jar BotswanaEMRAmbulance.jar
ENTRYPOINT ["java","-jar", "target/BotswanaEMRAmbulance.jar"]