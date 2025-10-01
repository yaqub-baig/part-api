# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot microservice called PartApi - part of the AutoAdda platform for automotive parts management. It's a REST API service built with Java 17, Spring Boot 2.7.14, and uses MySQL as the primary database.

## Development Commands

### Build and Run
- **Build project**: `mvn clean compile`
- **Run application locally**: `mvn spring-boot:run`
- **Build JAR**: `mvn clean package`
- **Run tests**: `mvn test`
- **Run specific test**: `mvn test -Dtest=TestClassName`

### Docker Development
- **Start services with Docker Compose**: `docker-compose up -d`
- **Stop services**: `docker-compose down`
- **Rebuild and start**: `docker-compose up --build`
- **View logs**: `docker-compose logs part-service`

The docker-compose setup includes:
- MySQL database on port 3308 (mapped from container port 3306)
- Part service on port 8060 (mapped from container port 8061)
- Database: `auto_adda_parts` with root credentials

### Development Server
- **Local server runs on**: `http://localhost:8060/api/v1`
- **Swagger UI**: `http://localhost:8060/api/v1/swagger-ui.html`
- **H2 Database Console** (for tests): Available when running tests with H2

## Architecture

### Package Structure
```
com.autoadda.apis.part/
├── config/          # Configuration classes (OpenAPI, FileStorage)
├── controller/      # REST controllers for all entities
├── entity/          # JPA entities
├── exception/       # Custom exception handling
├── mapper/          # MapStruct mappers for DTO conversions
├── repository/      # JPA repositories
├── request/         # Request DTOs
├── response/        # Response DTOs
├── service/         # Business logic services
├── util/            # Utility classes
└── validator/       # Custom validators
```

### Key Technologies
- **Framework**: Spring Boot 2.7.14 with Java 17
- **Database**: MySQL 8 (production), H2 (testing)
- **ORM**: Spring Data JPA with Hibernate
- **Documentation**: SpringDoc OpenAPI 3 (Swagger)
- **Service Discovery**: Netflix Eureka Client
- **Mapping**: MapStruct for DTO conversions
- **Testing**: JUnit 5, Mockito, Spring Boot Test

### Core Entities
The service manages automotive parts with entities including:
- Inventory, Orders, ShoppingCart
- Product, Category, Attributes
- Make, Model, Engine, VehicleType
- Manufacturer, Supplier, BrandOnboard
- MediaFile for file management

### Database Configuration
- **Local**: `jdbc:mysql://localhost:3306/auto_adda_parts`
- **Docker**: `jdbc:mysql://mysql-docker:3306/auto_adda_parts`
- **Credentials**: root/password (local), root/root123 (docker)
- **DDL**: `hibernate.ddl-auto=update`

### File Upload
- **Upload directory**: `../uploaded-files` (configurable via `file.upload-dir`)
- **Max file size**: 20MB
- **Max request size**: 50MB
- **Base URL**: Configured via `file.base-url`

### Inter-Service Communication
- **Parts service base URL**: `http://localhost:8060/api/v1`
- **Notification service**: `http://localhost:8093/api/v1/notification/send`
- **Auth service**: `http://localhost:8091/api/v1/auth/email/confirm`
- **Production URL**: `https://carserviceplatform-api.com`

### Testing
- Uses H2 in-memory database for tests
- Test classes located in `src/test/java/com/autoadda/apis/part/`
- JUnit 5 with Spring Boot Test and Mockito
- Test categories: service tests, integration tests