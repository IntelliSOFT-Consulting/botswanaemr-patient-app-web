FROM maven:3.6.3-jdk-11-slim AS build
FROM openjdk:11
EXPOSE 7000
WORKDIR /botswana_emr_facility_java_backend
COPY . /botswana_emr_facility_java_backend
ADD target/BotswanaEMRFacility.jar BotswanaEMRFacility.jar
ENTRYPOINT ["java","-jar", "target/BotswanaEMRFacility.jar"]