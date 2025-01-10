FROM openjdk:21-jdk

# Set working directory
WORKDIR /app

# Copy the application JAR and dependencies
COPY target/*.jar app.jar
COPY target/lib/*.jar lib/

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-cp", ".:lib/*", "app.jar"]
