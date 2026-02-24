FROM maven:3.6.3-jdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:11
EXPOSE 8081
WORKDIR /botswana_emr_auth_javabackend
COPY --from=build /app/target/BotswanaEMRAuthentication-0.0.1-SNAPSHOT.jar BotswanaEMRAuthentication-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","BotswanaEMRAuthentication-0.0.1-SNAPSHOT.jar"]