FROM maven:3.6.3-jdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:11
EXPOSE 7001
WORKDIR /botswana_notification_backend
COPY --from=build /app/target/BotswanaEMRNotifications-0.0.1-SNAPSHOT.jar BotswanaEMRNotifications.jar
ENTRYPOINT ["java","-jar","BotswanaEMRNotifications.jar"]