## Use Java 17 to match your pom.xml (not 21)
#FROM eclipse-temurin:17-jdk-alpine
#
## Set working directory inside container
#WORKDIR /app
#
## Copy the Spring Boot executable JAR with correct artifact name
#COPY target/PartApi-0.0.1-SNAPSHOT.jar app.jar
#
## Expose the default port
#EXPOSE 8060
## Set environment variable inside container
#ENV SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/auto_adda_parts?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
#
## Run the app
#ENTRYPOINT ["java", "-jar", "/app/app.jar"]
# Use Java 17 to match your pom.xml
FROM eclipse-temurin:17-jdk-alpine

# Set working directory inside container
WORKDIR /app

# Copy the Spring Boot executable JAR
COPY target/*.jar app.jar

# Expose the default port
EXPOSE 8060

# Set environment variables for PostgreSQL
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-d3a00ere5dus73brdljg-a.oregon-postgres.render.com:5432/part_db
ENV SPRING_DATASOURCE_USERNAME=part_db_user
ENV SPRING_DATASOURCE_PASSWORD=2PVmRkm9GiuJeep9osBfXVBfE8wyADqs
ENV SPRING_JPA_HIBERNATE_DDL_AUTO=update
ENV SPRING_JPA_SHOW_SQL=true

# Run the app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
