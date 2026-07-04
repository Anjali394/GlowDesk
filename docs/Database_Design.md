# Database Design Document

**Project:** GlowDesk — Salon Management Platform
**Version:** 1.0 (MVP)
**Database:** PostgreSQL 16 (Neon)

---

## Design Principles

- Normalized to 3NF
- **UUID primary keys** — `gen_random_uuid()` — required for multi-branch future and distributed safety
- Audit fields on all tables: `created_at`, `updated_at`, `created_by`, `updated_by`
- Flyway migrations for all schema changes — Hibernate `ddl-auto: validate` only
- Foreign key constraints enforced at DB level
- **Soft-delete:** `is_active BOOLEAN DEFAULT TRUE` on all business entities that may be referenced by historical appointments
- **Price snapshot:** `AppointmentService.price_at_booking` captures price at booking time — the live `Service.price` is never used for billing after the appointment is created

---

## Relationships

| Relationship | Cardinality | Notes |
|---|---|---|
| Brand → Branch | 1:N | |
| Branch → Stylist | 1:N | |
| Branch → Service | 1:N | |
| Branch → Appointment | 1:N | |
| User → Role | N:M | Via `user_roles` join table |
| User → Customer | 1:1 | |
| Customer → Appointment | 1:N | |
| Stylist → Appointment | 1:N | Assigned stylist |
| Combo → Service (ComboService) | N:M | Junction table |
| Appointment → AppointmentService | 1:N | |
| Service → AppointmentService | 1:N | |
| User → Notification | 1:N | |

---

## Table Definitions

### `brand`
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK, DEFAULT gen_random_uuid() |
| name | VARCHAR(100) | NOT NULL, UNIQUE |
| is_active | BOOLEAN | DEFAULT TRUE |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | |
| created_by | VARCHAR(100) | |
| updated_by | VARCHAR(100) | |

### `branch`
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| brand_id | UUID | FK → brand.id, NOT NULL |
| name | VARCHAR(100) | NOT NULL |
| address | TEXT | |
| phone | VARCHAR(20) | |
| opening_time | TIME | NOT NULL |
| closing_time | TIME | NOT NULL |
| is_active | BOOLEAN | DEFAULT TRUE |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | |
| created_by | VARCHAR(100) | |
| updated_by | VARCHAR(100) | |

### `role`
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| name | VARCHAR(50) | NOT NULL, UNIQUE |

Seed: `CUSTOMER`, `RECEPTIONIST`, `ADMIN`

### `user`
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| email | VARCHAR(150) | NOT NULL, UNIQUE |
| password_hash | VARCHAR(255) | NOT NULL |
| is_active | BOOLEAN | DEFAULT TRUE |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | |

### `user_roles` (join table)
| Column | Type | Constraints |
|---|---|---|
| user_id | UUID | FK → user.id, NOT NULL |
| role_id | UUID | FK → role.id, NOT NULL |
| PK | | (user_id, role_id) |

### `customer`
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| user_id | UUID | FK → user.id, NOT NULL, UNIQUE |
| first_name | VARCHAR(80) | NOT NULL |
| last_name | VARCHAR(80) | |
| phone | VARCHAR(20) | |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | |

### `stylist`
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| branch_id | UUID | FK → branch.id, NOT NULL |
| first_name | VARCHAR(80) | NOT NULL |
| last_name | VARCHAR(80) | |
| experience | INTEGER | years, NOT NULL |
| rating | DECIMAL(3,2) | DEFAULT 0.00 |
| is_active | BOOLEAN | DEFAULT TRUE |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | |
| created_by | VARCHAR(100) | |
| updated_by | VARCHAR(100) | |

### `service`
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| branch_id | UUID | FK → branch.id, NOT NULL |
| name | VARCHAR(100) | NOT NULL |
| description | TEXT | |
| duration | INTEGER | minutes, NOT NULL |
| price | DECIMAL(10,2) | NOT NULL |
| is_active | BOOLEAN | DEFAULT TRUE |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | |
| created_by | VARCHAR(100) | |
| updated_by | VARCHAR(100) | |

### `combo`
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| branch_id | UUID | FK → branch.id, NOT NULL |
| name | VARCHAR(100) | NOT NULL |
| description | TEXT | |
| discount_type | VARCHAR(20) | `PERCENTAGE` or `FIXED` |
| discount_value | DECIMAL(10,2) | NOT NULL |
| is_active | BOOLEAN | DEFAULT TRUE |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | |
| created_by | VARCHAR(100) | |
| updated_by | VARCHAR(100) | |

### `combo_service` (junction)
| Column | Type | Constraints |
|---|---|---|
| combo_id | UUID | FK → combo.id, NOT NULL |
| service_id | UUID | FK → service.id, NOT NULL |
| PK | | (combo_id, service_id) |

### `appointment`
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| branch_id | UUID | FK → branch.id, NOT NULL |
| customer_id | UUID | FK → customer.id, NOT NULL |
| stylist_id | UUID | FK → stylist.id, NOT NULL |
| combo_id | UUID | FK → combo.id, NULLABLE |
| status | VARCHAR(20) | NOT NULL |
| scheduled_date | DATE | NOT NULL |
| start_time | TIME | NOT NULL |
| end_time | TIME | NOT NULL (calculated at booking) |
| total_price | DECIMAL(10,2) | NOT NULL |
| rejection_reason | TEXT | NULLABLE |
| expires_at | TIMESTAMPTZ | NOT NULL (created_at + 30 min) |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | |

### `appointment_service`
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| appointment_id | UUID | FK → appointment.id, NOT NULL |
| service_id | UUID | FK → service.id, NOT NULL |
| price_at_booking | DECIMAL(10,2) | NOT NULL — price snapshot |
| duration | INTEGER | minutes — duration snapshot |

### `notification`
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| user_id | UUID | FK → user.id, NOT NULL |
| type | VARCHAR(50) | NOT NULL (e.g. `BOOKING_CONFIRMED`) |
| title | VARCHAR(150) | NOT NULL |
| message | TEXT | NOT NULL |
| is_read | BOOLEAN | DEFAULT FALSE |
| read_at | TIMESTAMPTZ | NULLABLE |
| created_at | TIMESTAMPTZ | NOT NULL |

---

## Index Strategy

| Table | Index Columns | Reason |
|---|---|---|
| appointment | (branch_id, scheduled_date, status) | Most common staff query pattern |
| appointment | (stylist_id, scheduled_date) | Availability and conflict checks |
| appointment | (customer_id) | Customer history lookup |
| appointment | (status, expires_at) | Scheduler — find PENDING for expiry |
| notification | (user_id, is_read) | Unread notification polling |
| stylist | (branch_id, is_active) | Active stylist listings |
| service | (branch_id, is_active) | Service catalog browsing |

---

## Soft Delete Policy

| Entity | Strategy | Reason |
|---|---|---|
| Stylist | `is_active = false` | Referenced by historical appointments |
| Service | `is_active = false` | Referenced by AppointmentService rows |
| Combo | `is_active = false` | Referenced by appointment rows |
| User | `is_active = false` | Disable auth without data loss |
| Branch | `is_active = false` | Multi-branch future safety |
| Role | Hard delete allowed | Only if no user_roles references exist |

---

## Flyway Migration Files

```
src/main/resources/db/migration/
├── V1__create_brand_branch.sql
├── V2__create_user_role.sql
├── V3__create_customer_stylist.sql
├── V4__create_service_combo.sql
├── V5__create_appointment.sql
├── V6__create_notification.sql
└── V7__seed_roles.sql
```

**Rule:** Never edit a migration file after it has been run. To change something, create a new migration file.

---

## ER Diagram

See `docs/ER_Diagram.png` — to be generated after all migrations are written.
