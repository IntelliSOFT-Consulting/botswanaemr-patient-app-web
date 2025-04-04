FROM maven:3.6.3-jdk-11-slim AS build
FROM openjdk:11
EXPOSE 7001
WORKDIR /botswana_notification_backend
COPY . /botswana_notification_backend
ADD target/BotswanaEMRNotifications.jar BotswanaEMRNotifications.jar
ENTRYPOINT ["java","-jar", "target/BotswanaEMRNotifications.jar"]