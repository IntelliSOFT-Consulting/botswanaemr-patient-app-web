FROM maven:3.6.3-jdk-11-slim AS build
FROM openjdk:11
EXPOSE 8082
WORKDIR /botswana_emr_appointments_java_backend
COPY . /botswana_emr_appointments_java_backend
ADD target/BotswanaEMRAppointments-0.0.1-SNAPSHOT.jar BotswanaEMRAppointments-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar", "target/BotswanaEMRAppointments-0.0.1-SNAPSHOT.jar"]