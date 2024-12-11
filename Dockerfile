# Use Maven with OpenJDK 21 to build the project
FROM maven:3.9.8-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and source code into the container
COPY pom.xml ./
COPY src ./src/

# Run Maven clean package to build the project
RUN mvn clean package

# Use a smaller image to run the application (OpenJDK 21)
FROM openjdk:21-jdk-slim

# Set environment variables for headless operation if needed (not usually required for Spring Boot apps)
#ENV JAVA_OPTS="-Djava.awt.headless=true"

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from the previous build stage (from the 'build' stage)
COPY --from=build /app/target/NoteKipzler-1.0-SNAPSHOT.jar /app/NoteKipzler-1.0-SNAPSHOT.jar

# Expose the port that Spring Boot runs on
EXPOSE 8081

# Command to run the application
ENTRYPOINT ["java", "-jar", "/app/NoteKipzler-1.0-SNAPSHOT.jar"]
