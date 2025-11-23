# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) and Cursor when working with code in this repository.

## Project Overview

LangIA is an AI-powered language learning platform built with Spring Boot 3.5.7 (Java 17), PostgreSQL 15, and Redis. Currently implements user registration with plans for microservices architecture.

## Build & Development Commands

```bash
# Build
./mvnw clean compile                    # Compile
./mvnw clean package                    # Build JAR
./mvnw clean package -DskipTests        # Build without tests

# Test
./mvnw clean test                       # Run all tests

# Run
./mvnw spring-boot:run                  # Start application locally

# Code quality
./mvnw checkstyle:check                 # Check code style
```

## Architecture

**Package structure:** `src/main/java/com/langia/backend/`
- `controller/` - REST endpoints (delegate only, no logic)
- `service/` - Business logic with mandatory logging
- `repository/` - JpaRepository interfaces
- `model/` - JPA entities (never return directly)
- `dto/` - Request/Response objects (always use)
- `exception/` - Custom exceptions + @ControllerAdvice
- `config/` - Spring configuration beans

**Request flow:** Controller → Service → Repository → PostgreSQL

## Coding Standards (from .cursor/rules/)

- **Language:** Code and variable names in English
- **DTOs:** Mandatory for all input/output (never return JPA entities)
- **Validation:** Always use @NotBlank, @Email, @Size on DTOs
- **Controllers:** HTTP verb handlers only - delegate to services
- **Services:** All business logic here, logs mandatory
- **Exceptions:** Handle via global @ControllerAdvice with JSON response
- **Config:** Use environment variables, no hardcoded values
- **Security:** BCrypt encoder factor 12

## Key Configuration

- **App config:** `src/main/resources/application.properties`
- **Test config:** `src/test/resources/application-test.properties` (uses H2)
- **Environment:** `.env` file with POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD
- **Docker:** `docker-compose.yml` runs PostgreSQL

## Git Workflow

- **Branches:** `main` (prod), `develop`, `feature/*`, `fix/*`
- **Commits:** `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`

## Extended Documentation

Module-specific guidelines in `docs/`:
- `02-backend-spring.md` - Backend patterns
- `03-microservicos.md` - Planned microservices architecture
- `05-docker-deploy.md` - Docker & deployment
- `06-git-workflow.md` - Git conventions
