# GlowDesk — Salon Management REST API

A production-ready backend REST API for salon appointment management built with Java 21 and Spring Boot 3.5.

## Live Links

- **Swagger UI:** https://glowdesk-production-8407.up.railway.app/swagger-ui.html
- **Health Check:** https://glowdesk-production-8407.up.railway.app/actuator/health

## Tech Stack

- Java 21 (Virtual Threads)
- Spring Boot 3.5
- Spring Security 6 + JWT
- PostgreSQL 16 (Neon)
- Flyway migrations
- Docker + Railway (CI/CD)
- Swagger UI (springdoc-openapi)

## Features (MVP)

- Role-based authentication (CUSTOMER, RECEPTIONIST, ADMIN)
- Service & combo package management
- Appointment booking with conflict prevention
- Stylist auto-assignment by availability and rating
- Receptionist approval workflow with 30-min auto-expiry
- In-app notification system

## Local Setup

1. Clone the repo
2. Create `src/main/resources/application-local.yml` with your DB credentials (see `application.yml` for required keys)
3. Run:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```
4. Open http://localhost:8080/swagger-ui.html
