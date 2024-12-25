# Use JDK 21 as the base image
FROM openjdk:21-jdk

# Set working directory
WORKDIR /app

# Copy the JAR file and rename it to notification.jar
COPY target/user-service-0.0.1-SNAPSHOT.jar user-service.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "user-service.jar"]
