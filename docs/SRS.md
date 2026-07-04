# GlowDesk — Salon Management Platform

**Software Requirements Specification (SRS)**

- **Version:** 1.0 (MVP)
- **Project Type:** Backend REST API
- **Target Stack:** Java 21, Spring Boot 3.4.x, PostgreSQL (Neon), Railway

---

# 1. Purpose

GlowDesk is a web-based salon management platform that enables customers to browse services, view transparent pricing, and book appointments online. The system automatically assigns the best available stylist. Salon staff manage bookings through role-based workflows. Admins monitor business operations through a dashboard.

The MVP supports **one salon branch** but is architected for **future multi-branch franchise expansion** via `branch_id` on all relevant entities.

---

# 2. Goals

- Digital appointment booking with real-time availability
- Transparent pricing with combo packages
- Automatic stylist assignment based on availability
- Receptionist approval workflow with auto-expiry
- Double-booking and time conflict prevention
- In-app notification system
- Scalable, production-ready architecture

---

# 3. Stakeholders

| Role | Responsibilities |
|---|---|
| Customer | Browse services, book and cancel appointments |
| Receptionist | Confirm/reject bookings, mark appointments completed |
| Admin | Manage services, combos, stylists, view reports |

---

# 4. Scope (MVP)

## Customer
- Register / Login
- Browse services and combo packages
- View prices
- Book appointment (single services or combo)
- Cancel: PENDING anytime; CONFIRMED up to 1 hour before start
- Rate the stylist after a COMPLETED appointment (1–5 stars)
- View appointment history
- Receive in-app notifications

## Receptionist
- View pending bookings
- Confirm or reject bookings
- Mark appointment as COMPLETED when service finishes

## Admin
- CRUD services and combo packages
- CRUD stylists
- View all appointments
- View stylist performance dashboard

---

# 5. Functional Requirements

## FR-01 Authentication

- JWT-based stateless authentication
- Role-based authorization: `CUSTOMER`, `RECEPTIONIST`, `ADMIN`
- Password encrypted with BCrypt
- All protected endpoints require `Authorization: Bearer <token>` header
- All endpoints versioned under `/api/v1/`

## FR-02 Service Management

Each service contains: Name, Description, Duration (minutes), Price (BigDecimal), Status (ACTIVE / INACTIVE), Soft-delete (`is_active` flag).

Initial seed services:
- Hair Cut — 30 min
- Wax — 45 min
- Face Cleanup — 60 min

## FR-03 Combo Packages

Admin creates promotional combinations of existing services.

Each combo contains: Name, Description, Discount (PERCENTAGE or FIXED), List of services (via `ComboService` mapping), Status (ACTIVE / INACTIVE).

A customer can book a combo directly. The system expands the combo into individual `AppointmentService` rows at booking time, capturing `price_at_booking` per service.

## FR-04 Stylist Management

Each stylist has: Name, Experience (years), Rating (average, updated after each customer rating), Branch, `is_active` soft-delete flag. Stylists have no user account and cannot log in — they are staff records managed by Admin.

## FR-05 Appointment Booking

Customer selects: One or more services OR one combo, Preferred date and time slot.

System behavior at booking time:
1. Resolve services (or expand combo into services).
2. Calculate `total_duration` = sum of all service durations.
3. Calculate `end_time = start_time + total_duration`.
4. Validate within branch operating hours.
5. Auto-assign stylist (see FR-06).
6. Capture `price_at_booking` per service — snapshot of current price, never updated.
7. Create appointment with status `PENDING`.
8. Set `expires_at = created_at + 30 minutes`.
9. Trigger notifications: Customer (BOOKING_CREATED) + Receptionist (NEW_BOOKING).

## FR-06 Stylist Assignment

**Always auto-assigned — customer does not select a stylist.**

- Find all active stylists at the branch with no overlapping appointment in the time window.
- Assign the one with the highest rating.
- If no available stylist exists: return `400` — `"No available stylist for the requested time slot"`. Do not create a PENDING booking without a stylist.

## FR-07 Rating

- Only available on COMPLETED appointments.
- Customer submits a rating of 1–5 stars via `POST /api/v1/appointments/{id}/rate`.
- Each appointment can only be rated once.
- The stylist's `rating` column is recalculated as an average of all ratings received.

## FR-08 Receptionist Approval

Receptionist must Confirm or Reject a PENDING booking within **30 minutes**.

| Action | Result | Notifications |
|---|---|---|
| Confirm | Status → CONFIRMED | Customer notified |
| Reject | Status → REJECTED | Customer notified (with optional reason) |
| No action in 30 min | Status → EXPIRED (scheduler) | Customer notified |

## FR-09 Appointment Status State Machine

```
PENDING
  ├── CONFIRMED    (Receptionist confirms within 30 min)
  │     ├── COMPLETED    (Receptionist marks done)
  │     └── CANCELLED    (Customer cancels ≥ 1 hr before start)
  ├── REJECTED     (Receptionist rejects)
  └── EXPIRED      (Scheduler — 30 min timeout, no receptionist action)
```

Status transitions:

| From | To | Actor | Condition |
|---|---|---|---|
| PENDING | CONFIRMED | Receptionist | — |
| PENDING | REJECTED | Receptionist | — |
| PENDING | EXPIRED | Scheduler | 30 min elapsed with no action |
| PENDING | CANCELLED | Customer | Any time |
| CONFIRMED | COMPLETED | Receptionist | Service finished |
| CONFIRMED | CANCELLED | Customer | Cancellation ≥ 1 hour before start |

## FR-10 Customer Cancellation

- PENDING appointments: cancellable at any time.
- CONFIRMED appointments: cancellable if `start_time - now ≥ 1 hour`. Within 1 hour: `400` error returned.
- Status transitions to `CANCELLED`.
- Receptionist is notified.

## FR-11 Notifications

All notifications are **in-app only** for MVP (stored in `notification` table). Email / SMS is planned for Phase 2.

| Event | Notified |
|---|---|
| Booking created | Customer, Receptionist |
| Booking confirmed | Customer |
| Booking rejected | Customer |
| Booking expired (auto) | Customer |
| Booking completed | Customer |
| Booking cancelled | Receptionist |

## FR-12 Admin Dashboard

Admin can view:
- Today's bookings (count + list)
- Bookings grouped by status
- Completed bookings (daily / weekly)
- Popular services (by booking count)
- Stylist performance (completed appointments, average rating)

---

# 6. Non-Functional Requirements

## Performance
- Average API response < 500 ms under normal load
- Virtual threads (`spring.threads.virtual.enabled: true`) for concurrency

## Security
- JWT tokens (access only, MVP)
- BCrypt password hashing
- HTTPS enforced in production
- Role-based access control on all endpoints

## Scalability
- One branch initially
- `branch_id` present on all branch-scoped entities for multi-branch expansion

## Availability
- 99% uptime target (Railway deployment)

## Input Validation
- All request bodies validated with Jakarta Bean Validation (`@Valid`, `@NotNull`, `@Size`, etc.)
- Invalid requests return `400 Bad Request` with field-level error detail

## Error Handling
- RFC 7807 Problem Details standard (Spring Boot 3 built-in, `spring.mvc.problemdetails.enabled: true`)
- Consistent error format across all endpoints

---

# 7. Technology Stack

| Layer | Technology | Decision Rationale |
|---|---|---|
| Language | Java 21 | LTS, virtual threads, modern features |
| Framework | Spring Boot 3.4.x | Auto-config, production-ready ecosystem |
| Concurrency | Virtual Threads (Java 21) | High throughput, zero extra cost |
| Security | Spring Security 6 + JWT (jjwt 0.12.x) | Stateless, role-based, industry standard |
| Database | PostgreSQL 16 (Neon) | Free managed cloud Postgres |
| ORM | Spring Data JPA + Hibernate 6 | Standard JPA, powerful query support |
| DB Migration | Flyway | Version-controlled schema, production-safe |
| Mapping | MapStruct | Compile-time DTO mapping, no reflection |
| Boilerplate | Lombok | Eliminates getters/setters/constructors/builders |
| Validation | Jakarta Bean Validation | Standard constraint annotations |
| Error Handling | RFC 7807 Problem Details | Industry-standard, built into Spring Boot 3 |
| Documentation | springdoc-openapi 2.x | Auto-generates Swagger UI from annotations |
| Testing | JUnit 5 + Mockito + Testcontainers | Unit + real DB integration tests |
| Monitoring | Spring Boot Actuator | Health check for Railway (`/actuator/health`) |
| Build | Maven 3.9.x | Standard, wide IDE support |
| Deployment | Railway | Auto-deploy from GitHub push |
| Containerization | Docker | Consistent local + production environment |

### Why springdoc-openapi over Springfox?

Springfox was last updated in 2021 and does not support Spring Boot 3 or Spring Security 6. `springdoc-openapi-starter-webmvc-ui` is the correct, actively maintained library for Spring Boot 3.

### Why RFC 7807 Problem Details over custom error format?

Spring Boot 3 includes built-in Problem Details support enabled by one config line. It produces a standard format that any API consumer recognizes, removing the need for a custom `GlobalExceptionHandler` for most cases.

### Why Virtual Threads?

Java 21 virtual threads handle blocking I/O (database calls, network) without consuming an OS thread per request. Enabled with one config line — zero code changes, significantly higher throughput for a booking API where most time is spent waiting on the database.

---

# 8. Core Entities

| Entity | Purpose |
|---|---|
| Brand | Salon brand (top-level owner) |
| Branch | Physical salon location |
| User | Authentication and role assignment |
| Role | Authorization roles |
| Customer | Customer profile (1:1 with User) |
| Stylist | Employee profile (staff record, no login) |
| Service | Individual salon services |
| Combo | Promotional combo package |
| ComboService | Services within a combo (N:M junction) |
| Appointment | Booking record |
| AppointmentService | Services booked in an appointment |
| Notification | In-app notification records |

---

# 9. Testing Strategy

| Layer | Type | Tooling |
|---|---|---|
| Service | Unit tests | JUnit 5 + Mockito |
| Repository | Integration tests | JUnit 5 + Testcontainers (PostgreSQL) |
| Controller | Slice tests | MockMvc + `@WebMvcTest` |

Testcontainers uses `@ServiceConnection` (Spring Boot 3.1+) for zero-config test database wiring.

**Coverage goal:** All service-layer business logic (booking conflict detection, duration calculation, auto-assign, status transitions, cancellation eligibility) must have unit test coverage.

---

# 10. Future Roadmap

## Phase 2
- Payments (Razorpay / Stripe)
- Memberships & Coupons
- Email / SMS notifications (Mailgun / Twilio)

## Phase 3
- Inventory management
- Billing & Invoicing
- Payroll & Employee commission

## Phase 4
- Multi-branch support
- Franchise dashboard
- Advanced analytics

## Phase 5
- Redis caching (service catalog, stylist availability)
- Kafka event streaming
- Mobile APIs
- AI-based stylist recommendation
