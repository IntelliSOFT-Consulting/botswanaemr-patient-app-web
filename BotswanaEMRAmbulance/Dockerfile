FROM maven:3.6.3-jdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:11
EXPOSE 7002
WORKDIR /botswana_emr_ambulance_java_backend
COPY --from=build /app/target/BotswanaEMRAmbulance-0.0.1-SNAPSHOT.jar BotswanaEMRAmbulance.jar
ENTRYPOINT ["java","-jar","BotswanaEMRAmbulance.jar"]