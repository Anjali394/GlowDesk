# GlowDesk — UML Diagrams

---

## 1. Entity Relationship Diagram (Database)

```mermaid
erDiagram
    BRAND {
        UUID id PK
        VARCHAR name
        BOOLEAN is_active
        TIMESTAMPTZ created_at
    }

    BRANCH {
        UUID id PK
        UUID brand_id FK
        VARCHAR name
        TEXT address
        VARCHAR phone
        TIME opening_time
        TIME closing_time
        BOOLEAN is_active
    }

    ROLE {
        UUID id PK
        VARCHAR name
    }

    USER {
        UUID id PK
        VARCHAR email
        VARCHAR password_hash
        BOOLEAN is_active
        TIMESTAMPTZ created_at
    }

    USER_ROLES {
        UUID user_id FK
        UUID role_id FK
    }

    CUSTOMER {
        UUID id PK
        UUID user_id FK
        VARCHAR first_name
        VARCHAR last_name
        VARCHAR phone
    }

    STYLIST {
        UUID id PK
        UUID branch_id FK
        VARCHAR first_name
        VARCHAR last_name
        INTEGER experience
        DECIMAL rating
        BOOLEAN is_active
    }

    SERVICE {
        UUID id PK
        UUID branch_id FK
        VARCHAR name
        TEXT description
        INTEGER duration
        DECIMAL price
        BOOLEAN is_active
    }

    COMBO {
        UUID id PK
        UUID branch_id FK
        VARCHAR name
        VARCHAR discount_type
        DECIMAL discount_value
        BOOLEAN is_active
    }

    COMBO_SERVICE {
        UUID combo_id FK
        UUID service_id FK
    }

    APPOINTMENT {
        UUID id PK
        UUID branch_id FK
        UUID customer_id FK
        UUID stylist_id FK
        UUID combo_id FK
        VARCHAR status
        DATE scheduled_date
        TIME start_time
        TIME end_time
        DECIMAL total_price
        TEXT rejection_reason
        TIMESTAMPTZ expires_at
    }

    APPOINTMENT_SERVICE {
        UUID id PK
        UUID appointment_id FK
        UUID service_id FK
        DECIMAL price_at_booking
        INTEGER duration
    }

    NOTIFICATION {
        UUID id PK
        UUID user_id FK
        VARCHAR type
        VARCHAR title
        TEXT message
        BOOLEAN is_read
        TIMESTAMPTZ created_at
    }

    BRAND       ||--o{ BRANCH           : "has"
    BRANCH      ||--o{ STYLIST          : "employs"
    BRANCH      ||--o{ SERVICE          : "offers"
    BRANCH      ||--o{ COMBO            : "offers"
    BRANCH      ||--o{ APPOINTMENT      : "hosts"

    USER        ||--|| CUSTOMER         : "is a"
    USER        ||--o{ USER_ROLES       : "has"
    ROLE        ||--o{ USER_ROLES       : "assigned to"
    USER        ||--o{ NOTIFICATION     : "receives"

    CUSTOMER    ||--o{ APPOINTMENT      : "books"
    STYLIST     ||--o{ APPOINTMENT      : "handles"

    COMBO       ||--o{ COMBO_SERVICE    : "contains"
    SERVICE     ||--o{ COMBO_SERVICE    : "included in"

    APPOINTMENT ||--o{ APPOINTMENT_SERVICE : "includes"
    SERVICE     ||--o{ APPOINTMENT_SERVICE : "booked as"
    COMBO       |o--o{ APPOINTMENT      : "used in"
```

---

## 2. Appointment Status Flow (State Diagram)

```mermaid
stateDiagram-v2
    [*] --> PENDING : Customer books

    PENDING --> CONFIRMED : Receptionist confirms
    PENDING --> REJECTED  : Receptionist rejects
    PENDING --> EXPIRED   : Scheduler (30 min timeout)
    PENDING --> CANCELLED : Customer cancels

    CONFIRMED --> COMPLETED : Receptionist marks done
    CONFIRMED --> CANCELLED : Customer cancels (≥ 1 hr before)

    REJECTED  --> [*]
    EXPIRED   --> [*]
    CANCELLED --> [*]
    COMPLETED --> [*]
```

---

## 3. Use Case Diagram

```mermaid
graph LR
    C([Customer])
    R([Receptionist])
    A([Admin])

    subgraph Authentication
        UC1[Register]
        UC2[Login]
    end

    subgraph Booking
        UC3[Browse Services & Combos]
        UC4[Book Appointment]
        UC5[Cancel Appointment]
        UC6[View Appointment History]
        UC7[Rate Stylist after Completion]
    end

    subgraph Receptionist Actions
        UC8[View Pending Bookings]
        UC9[Confirm Booking]
        UC10[Reject Booking]
        UC11[Mark Completed]
    end

    subgraph Admin Actions
        UC12[Manage Services]
        UC13[Manage Combos]
        UC14[Manage Stylists]
        UC15[View All Appointments]
    end

    subgraph Notifications
        UC16[View Notifications]
    end

    C --> UC1
    C --> UC2
    C --> UC3
    C --> UC4
    C --> UC5
    C --> UC6
    C --> UC7
    C --> UC16

    R --> UC2
    R --> UC8
    R --> UC9
    R --> UC10
    R --> UC11
    R --> UC16

    A --> UC2
    A --> UC12
    A --> UC13
    A --> UC14
    A --> UC15
    A --> UC16
```

---

## 4. Layered Architecture Diagram

```mermaid
graph TD
    Client["Client (Swagger UI / Mobile App / Web)"]

    subgraph Spring Boot API
        Controller["Controller Layer\n(REST endpoints, input validation)"]
        Service["Service Layer\n(business logic, status rules)"]
        Repository["Repository Layer\n(Spring Data JPA)"]
        Security["Security Layer\n(JWT filter, role checks)"]
        Scheduler["Scheduler\n(auto-expire PENDING appointments)"]
        Notification["Notification Module\n(in-app notifications)"]
    end

    DB[(PostgreSQL\nNeon Cloud)]

    Client -->|HTTPS request| Security
    Security --> Controller
    Controller --> Service
    Service --> Repository
    Service --> Notification
    Scheduler --> Service
    Repository --> DB
    Notification --> Repository
```
