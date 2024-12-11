# Use Maven with OpenJDK 21 to build the project
FROM maven:3.9.8-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and source code into the container
COPY pom.xml ./
COPY src ./src/

# Install necessary libraries to avoid headless errors (if required for GUI apps)
RUN apt-get update && apt-get install -y \
    libxext6 \
    libxrender1 \
    libx11-6 \
    libxtst6 \
    libxi6 \
    libgdk-pixbuf2.0-0

# Set the JAVA_OPTS to ensure headless mode if using Java GUI libraries like AWT/Swing
#ENV JAVA_OPTS="-Djava.awt.headless=true"

# Run Maven clean package to build the project
RUN mvn clean package -DskipTests

# Use a smaller image to run the application (OpenJDK 21)
FROM openjdk:21-jdk-slim

# Set environment variables for headless operation
#ENV JAVA_OPTS="-Djava.awt.headless=true"

# Copy the JAR file from the previous build stage (from the 'build' stage)
COPY --from=build /app/target/NoteKipzler-1.0-SNAPSHOT.jar /app/NoteKipzler-1.0-SNAPSHOT.jar

# Command to run the application
ENTRYPOINT Xvfb :99 & \
               export DISPLAY=:99 && \
               java, -jar, /app/NoteKipzler-1.0-SNAPSHOT.jar
