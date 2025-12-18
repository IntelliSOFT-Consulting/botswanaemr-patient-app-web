FROM maven:3.6.3-jdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:11
EXPOSE 7003
WORKDIR /file_storage
COPY --from=build /app/target/BotswanaEMRFileStorage-0.0.1-SNAPSHOT.jar FileStorage.jar
ENTRYPOINT ["java","-jar","FileStorage.jar"]