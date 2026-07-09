# GlowDesk — Salon Management Platform

> A production-grade **Salon Management Platform** REST API built with **Java 21** and **Spring Boot 3**.
> Designed for a **single salon branch in the MVP** with the architecture ready to scale to
> **multi-branch franchise management** in future releases.

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-Portfolio-lightgrey)](#license)

---

## What is GlowDesk?

GlowDesk is a **white-label salon management system** — think Domino's own ordering platform, but for a salon brand. Unlike marketplace apps (Fresha, Booksy), GlowDesk is built and owned by the salon.

Customers book appointments online. The system auto-assigns the best available stylist. Receptionists confirm bookings and mark them complete. Admins manage the business. All through a clean REST API with Swagger documentation.

---

## Live Demo

| Resource | URL |
|---|---|
| API Base | `https://glowdesk-production-8407.up.railway.app/api/v1` |
| Swagger UI | `https://glowdesk-production-8407.up.railway.app/swagger-ui.html` |
| Health Check | `https://glowdesk-production-8407.up.railway.app/actuator/health` |

> Hosted on Railway. Database on Neon PostgreSQL.

---

## Features

### Customer
- Register and login (JWT)
- Browse services and combo packages with live pricing
- Book an appointment (single services or combo)
- Cancel an appointment (PENDING anytime; CONFIRMED up to 1 hour before)
- View appointment history
- Rate the stylist after a COMPLETED appointment
- Receive in-app notifications
- Receive email confirmation and 30-minute pre-appointment reminder

### Receptionist
- View pending bookings queue
- Confirm or reject bookings
- Mark appointment as COMPLETED when done

### Admin
- Full CRUD on services, combos, and stylists
- View all appointments with filtering
- Stylist performance dashboard

---

## Appointment Status Flow

```
PENDING ──► CONFIRMED ──► COMPLETED
   │              └──► CANCELLED  (customer, ≥ 1 hr before start)
   ├──► REJECTED  (receptionist)
   └──► EXPIRED   (scheduler, 30 min timeout)
```

---

## Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| Language | Java 21 | Virtual threads, modern syntax |
| Framework | Spring Boot 3.5.x | Auto-configuration, production-ready |
| Security | Spring Security 6 + JWT (jjwt 0.12.x) | Stateless auth, role-based access |
| Database | PostgreSQL 16 (Neon) | Managed cloud Postgres |
| ORM | Spring Data JPA + Hibernate 6 | Repositories, JPQL |
| Migration | Flyway | Version-controlled schema |
| Mapping | MapStruct | Compile-time DTO mapping |
| Boilerplate | Lombok | Annotations instead of boilerplate code |
| Validation | Jakarta Bean Validation | `@Valid`, `@NotNull`, `@Size` |
| Error Handling | RFC 7807 Problem Details | Industry-standard error responses |
| Documentation | springdoc-openapi 2.x | Auto-generated Swagger UI |
| Email | Spring Boot Mail (Gmail SMTP) | Async confirmation + reminder emails |
| Testing | JUnit 5 + Mockito + Testcontainers | Unit and integration tests |
| Monitoring | Spring Boot Actuator | Health checks for Railway |
| Build | Maven | Dependency management |
| Deployment | Railway | PaaS, auto-deploy from GitHub |
| Container | Docker | Consistent environments |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/glowdesk/api/
│   │   ├── config/          # Spring beans, security config, OpenAPI config
│   │   ├── controller/      # REST controllers per module
│   │   ├── dto/
│   │   │   ├── request/     # Incoming request bodies
│   │   │   └── response/    # Outgoing response bodies
│   │   ├── entity/          # JPA entities
│   │   ├── exception/       # Custom exceptions + GlobalExceptionHandler
│   │   ├── mapper/          # MapStruct mappers
│   │   ├── repository/      # Spring Data JPA repositories
│   │   ├── security/        # JWT filter, UserDetailsService, token utils
│   │   ├── service/         # Business logic
│   │   ├── notification/    # In-app notification creation + retrieval
│   │   ├── scheduler/       # Auto-expiry of PENDING appointments
│   │   └── util/            # Shared utilities
│   └── resources/
│       ├── application.yml
│       └── db/migration/    # Flyway SQL migrations
└── test/
    └── java/com/glowdesk/api/
        ├── service/         # Unit tests (JUnit 5 + Mockito)
        ├── repository/      # Integration tests (Testcontainers)
        └── controller/      # Slice tests (MockMvc)
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker (optional, for Testcontainers)
- A Neon PostgreSQL database (free at [neon.tech](https://neon.tech))

### Local Setup

1. Clone the repo and navigate to the backend:
   ```bash
   git clone https://github.com/Anjali394/GlowDesk.git
   cd GlowDesk/backend/api
   ```

2. Create `src/main/resources/application-local.yml` with your credentials:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://<your-neon-host>/glowdesk?sslmode=require
       username: <user>
       password: <password>
     mail:
       username: your_gmail@gmail.com
       password: your_gmail_app_password   # Gmail App Password, not account password
   jwt:
     secret: your-local-dev-secret-key-32chars
   ```

3. Run with the local profile:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

App starts at `http://localhost:8080`  
Swagger UI at `http://localhost:8080/swagger-ui.html`  
Health check at `http://localhost:8080/actuator/health`

### Run Tests

```bash
mvn test
```

Integration tests use Testcontainers — Docker must be running.

### Environment Variables (production / Railway)

| Variable | Description |
|---|---|
| `DB_URL` | JDBC URL for PostgreSQL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | 256-bit JWT signing secret |
| `JWT_EXPIRATION` | Token expiry in ms (default: `86400000`) |
| `MAIL_USERNAME` | Gmail address used as sender |
| `MAIL_PASSWORD` | Gmail App Password |

---

## API Overview

All endpoints versioned under `/api/v1/`.

```
POST   /api/v1/auth/register
POST   /api/v1/auth/login

GET    /api/v1/services
POST   /api/v1/services                    [ADMIN]
PUT    /api/v1/services/{id}               [ADMIN]
DELETE /api/v1/services/{id}               [ADMIN]

GET    /api/v1/combos
POST   /api/v1/combos                      [ADMIN]

GET    /api/v1/stylists
POST   /api/v1/stylists                    [ADMIN]

POST   /api/v1/appointments
GET    /api/v1/appointments                [CUSTOMER — own history]
PATCH  /api/v1/appointments/{id}/confirm   [RECEPTIONIST]
PATCH  /api/v1/appointments/{id}/reject    [RECEPTIONIST]
PATCH  /api/v1/appointments/{id}/complete  [RECEPTIONIST]
PATCH  /api/v1/appointments/{id}/cancel    [CUSTOMER]
POST   /api/v1/appointments/{id}/rate      [CUSTOMER]

GET    /api/v1/notifications               [AUTHENTICATED]
```

Full interactive docs available at `/swagger-ui/index.html`.

---

## Error Response Format

GlowDesk uses the RFC 7807 Problem Details standard (Spring Boot 3 built-in):

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "No available stylist for the requested services and time slot",
  "instance": "/api/v1/appointments"
}
```

---

## Roadmap

### Phase 1 — MVP (Current)
- [x] Authentication (JWT)
- [x] Service Catalog
- [x] Combo Packages
- [x] Stylist Management
- [x] Appointment Booking with auto-assign and conflict prevention
- [x] Receptionist Workflow
- [x] Stylist Rating
- [x] In-app Notifications
- [x] Auto-expiry Scheduler
- [x] Email Notifications (confirmation + 30-min reminder via Gmail SMTP)

### Phase 2
- [ ] Payments (Razorpay)
- [ ] Memberships & Coupons
- [ ] SMS Notifications (Twilio)

### Phase 3
- [ ] Inventory Management
- [ ] Billing & Invoicing
- [ ] Payroll & Commission

### Phase 4
- [ ] Multi-Branch Support
- [ ] Franchise Dashboard

### Phase 5
- [ ] Redis Caching
- [ ] Kafka Event Streaming
- [ ] AI-based Stylist Recommendation

---

## Documentation

| Document | Description |
|---|---|
| [SRS](docs/SRS.md) | Software Requirements Specification |
| [System Architecture](docs/System_Architecture.md) | Architecture decisions and module design |
| [Database Design](docs/Database_Design.md) | Table definitions, relationships, indexes |
| [UML Diagrams](docs/UML_Diagrams.md) | ER diagram, state machine, use case, architecture diagrams |

---

## License

This project is built for **learning, portfolio development, and future commercial expansion**.
Not licensed for redistribution.
