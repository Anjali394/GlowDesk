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
- Spring Boot Mail (Gmail SMTP)
- Docker + Railway (CI/CD)
- Swagger UI (springdoc-openapi)

## Features (MVP)

- Role-based authentication (CUSTOMER, RECEPTIONIST, ADMIN)
- Service & combo package management
- Appointment booking with conflict prevention
- Stylist auto-assignment by availability and rating
- Receptionist approval workflow with 30-min auto-expiry
- In-app notification system
- Email notifications: confirmation + 30-min pre-appointment reminder (async via Gmail SMTP)

## Local Setup

1. Clone the repo
2. Create `src/main/resources/application-local.yml` with your DB and mail credentials (see `application.yml` for required keys):
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://<host>/<db>?sslmode=require
       username: <user>
       password: <password>
     mail:
       username: your_gmail@gmail.com
       password: your_gmail_app_password
   jwt:
     secret: your-local-secret-key
   ```
3. Run:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```
4. Open http://localhost:8080/swagger-ui.html

## Environment Variables (production)

| Variable | Description |
|---|---|
| `DB_URL` | JDBC URL for PostgreSQL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | 256-bit JWT signing secret |
| `JWT_EXPIRATION` | Token expiry in ms (default: 86400000) |
| `MAIL_USERNAME` | Gmail address used as sender |
| `MAIL_PASSWORD` | Gmail App Password (not your account password) |

