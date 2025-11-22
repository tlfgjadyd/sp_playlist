# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Gradle wrapper and build files
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Grant execution permissions to the Gradle wrapper
RUN chmod +x ./gradlew

# Build the application
# This will also download dependencies
RUN ./gradlew build --no-daemon

# Expose the port the app runs on
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "build/libs/myplaylist-0.0.1-SNAPSHOT.jar"]
