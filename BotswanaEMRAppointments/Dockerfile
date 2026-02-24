FROM maven:3.6.3-jdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:11
EXPOSE 8082
WORKDIR /botswana_emr_appointments_java_backend
COPY --from=build /app/target/BotswanaEMRAppointments-0.0.1-SNAPSHOT.jar BotswanaEMRAppointments-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","BotswanaEMRAppointments-0.0.1-SNAPSHOT.jar"]