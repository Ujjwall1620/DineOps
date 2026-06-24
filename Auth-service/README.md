# Auth-service

## Overview

`Auth-service` is the authentication and authorization service for the DineOps platform. It handles user registration, login, password protection, JWT token generation, and security enforcement for the restaurant microservices ecosystem.

## Features

- User registration with email, username, password, and role
- Password hashing using Spring Security `PasswordEncoder`
- JWT token issuance for authenticated sessions
- Basic security configuration for public and protected routes
- Kafka-powered audit logging for authentication events

## Architecture

`Auth-service` is built with Spring Boot and includes the following core components:

- `AuthController` — exposes REST endpoints for registration and login
- `AuthService` — business logic for user persistence and authentication
- `JwtUtil` — creates signed JWT tokens and embeds user details
- `AuthRepository` — JPA repository for user storage
- `securityConfig` — Spring Security configuration for access control
- `User` entity — stores user details in the database

## Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Authenticate a user and return a JWT |

### Request payloads

`POST /auth/register`
```json
{
  "username": "john.doe",
  "email": "john.doe@example.com",
  "role": "USER",
  "password": "securePassword123"
}
```

`POST /auth/login`
```json
{
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```

### Response payloads

`POST /auth/login` returns:
```json
{
  "token": "eyJhbGciOiJI..."
}
```

## Data model

The service stores users using the `User` entity:

- `id` — generated primary key
- `username` — user display name
- `email` — unique login identifier
- `role` — user role, stored as enum string
- `password` — hashed password

## Configuration

The main configuration file is `src/main/resources/application.properties`.

Important settings include:

- `server.port` — service HTTP port (`8081`)
- `spring.datasource.*` — MySQL connection configuration
- `spring.jpa.hibernate.ddl-auto` — schema generation strategy
- `spring.kafka.bootstrap-servers` — Kafka broker address
- JWT secret and expiration are currently hardcoded in `JwtUtil`

> Note: For production, move the JWT secret to environment variables or a secure vault.

## Local development

From the `Auth-service` directory, start the service using the Maven wrapper:

```bash
./mvnw.cmd spring-boot:run
```

On Linux/macOS:

```bash
./mvnw spring-boot:run
```

## Dependencies

Key dependencies include:

- Spring Boot Starter Web
- Spring Boot Starter Security
- Spring Boot Starter Data JPA
- MySQL Connector/J
- Spring Kafka
- jjwt for JWT token support
- Lombok for boilerplate reduction

## Notes

- The service expects a MySQL database available on `localhost:3307`.
- Kafka is expected on `localhost:29092` for log publishing.
- The current implementation does not expose token revocation or refresh endpoints.

## Future improvements

- Add explicit JWT validation and token expiration checking
- Move secret configuration out of code and into externalized properties
- Add role-based authorization for protected API calls
- Add request validation and better error responses
