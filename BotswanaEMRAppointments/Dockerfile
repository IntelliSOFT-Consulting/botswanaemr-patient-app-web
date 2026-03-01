# Stage 1: Build the JAR
FROM maven:3.6.3-jdk-11-slim AS build
WORKDIR /app

# Copy only pom.xml first to leverage caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production image
FROM amazoncorretto:11
WORKDIR /botswana_emr_appointments_java_backend

# Expose port
EXPOSE 8082

# Copy built JAR from build stage
COPY --from=build /app/target/BotswanaEMRAppointments app.jar

# Run the JAR
ENTRYPOINT ["java","-jar","app.jar"]