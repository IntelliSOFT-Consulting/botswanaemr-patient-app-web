FROM maven:3.6.3-jdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:11
EXPOSE 7000
WORKDIR /botswana_emr_facility_java_backend
COPY --from=build /app/target/BotswanaEMRFacility-0.0.1-SNAPSHOT.jar BotswanaEMRFacility.jar
ENTRYPOINT ["java","-jar","BotswanaEMRFacility.jar"]