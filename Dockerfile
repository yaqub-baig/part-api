# Use a lightweight Java image
FROM eclipse-temurin:23-jdk-alpine

# Set working directory inside container
WORKDIR /app

# Copy the JAR from local target folder
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Expose the default port
EXPOSE 8097

# Run the app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
