# LedgerLine — Production Financial Transaction & Ledger Management Platform

> **Day 1 Milestone: Foundation, Database Architecture, Authentication & Role-Based Access Control**

LedgerLine is a secure, enterprise-grade financial transaction and double-entry ledger management system designed to manage financial accounts, permission governance, and auditable monetary records.

---

## 1. Technology Stack

### Frontend
- **Framework**: React 18 with Vite
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **Routing**: React Router v6
- **HTTP Client**: Axios (centralized interceptors, withCredentials)
- **Icons**: Lucide React
- **Data Visualization**: Recharts

### Backend
- **Platform**: Java 21 / 23 & Spring Boot 3.3.5
- **Security**: Spring Security 6, BCrypt password hashing, JWT (JJWT 0.12.6)
- **Data Persistence**: Spring Data JPA & Hibernate ORM
- **Validation**: Jakarta Bean Validation (`hibernate-validator`)
- **API Documentation**: Springdoc OpenAPI / Swagger UI
- **Build & Dependency Management**: Apache Maven 3.9+
- **Database Migrations**: Flyway Database Migrations (`flyway-mysql`)

### Database
- **Database Engine**: MySQL 8.0
- **Isolation & Testing**: In-memory H2 with MySQL dialect emulation for isolated test suites
- **Monetary Types**: Strictly `DECIMAL(19,2)` (no float or double)
- **Data Integrity**: Foreign keys with cascade rules, composite keys, check constraints (`balance >= 0.00`)

---

## 2. Directory Structure

```
LedgerLine/
│
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   │   ├── common/       # Button, Input, Badge, Card
│   │   │   ├── layout/       # Navbar, Sidebar, MainLayout
│   │   │   └── auth/         # ProtectedRoute
│   │   ├── pages/
│   │   │   ├── auth/         # LoginPage, RegisterPage
│   │   │   ├── dashboard/    # DashboardPage
│   │   │   ├── profile/      # ProfilePage
│   │   │   └── errors/       # UnauthorizedPage, NotFoundPage
│   │   ├── services/         # api.ts, authService, userService, accountService
│   │   ├── context/          # AuthContext.tsx
│   │   ├── hooks/            # useAuth.ts
│   │   ├── routes/           # AppRoutes.tsx
│   │   ├── types/            # index.ts
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── package.json
│   ├── vite.config.ts
│   └── tailwind.config.js
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ledgerline/
│   │   │   │   ├── config/       # SecurityConfig, OpenApiConfig, DataInitializer
│   │   │   │   ├── controller/   # Auth, User, Admin, Account controllers
│   │   │   │   ├── service/      # AuthService, UserService, AccountService, AuditLogService
│   │   │   │   ├── repository/   # User, Role, Account, AccountPermission, Audit, Transaction
│   │   │   │   ├── entity/       # JPA Entities
│   │   │   │   ├── dto/          # Requests & Responses
│   │   │   │   ├── security/     # JwtTokenProvider, JwtFilter, CustomUserDetails
│   │   │   │   ├── exception/    # GlobalExceptionHandler & custom exceptions
│   │   │   │   ├── mapper/       # Entity-to-DTO mappers
│   │   │   │   └── LedgerLineApplication.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-test.yml
│   │   │       └── db/migration/ # Flyway V1, V2 migrations
│   │   └── test/                 # 19 automated integration & unit tests
│   └── pom.xml
│
├── database/
│   ├── README.md
│   └── schema-documentation.md
│
├── docs/
│   ├── ARCHITECTURE.md
│   └── API_DOCUMENTATION.md
│
├── THREATS.md
├── PROMPTS.md
├── README.md
├── .gitignore
├── .env.example
└── docker-compose.yml
```

---

## 3. Getting Started & Windows Execution Commands

### Prerequisites
- Java 21+ (`java -version`)
- Node.js 18+ and npm (`node -v`, `npm -v`)
- MySQL 8.0 or Docker

### Step 1: Configure Environment Variables
Copy `.env.example` to `.env`:
```powershell
Copy-Item .env.example .env
```

### Step 2: Database Setup

**Option A (Docker Compose - Recommended):**
```powershell
docker compose up -d
```

**Option B (Local MySQL Server):**
Create the database and grant privileges:
```sql
CREATE DATABASE ledgerline_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ledger_user'@'localhost' IDENTIFIED BY 'ledger_secure_password_replace_me';
GRANT ALL PRIVILEGES ON ledgerline_db.* TO 'ledger_user'@'localhost';
FLUSH PRIVILEGES;
```

### Step 3: Compile Backend & Run Automated Tests
```powershell
Set-Location "d:\LedgerLine\backend"
mvn clean test
```
*Expected result*: `Tests run: 19, Failures: 0, Errors: 0, Skipped: 0` — `BUILD SUCCESS`.

### Step 4: Start Backend Application
```powershell
Set-Location "d:\LedgerLine\backend"
mvn spring-boot:run
```
Backend will start on `http://localhost:8080`.  
Swagger UI is accessible at: `http://localhost:8080/swagger-ui.html`.

### Step 5: Install Frontend Dependencies & Start UI
In a new PowerShell terminal:
```powershell
Set-Location "d:\LedgerLine\frontend"
npm install
npm run dev
```
Frontend will be running on `http://localhost:5173`.

---

## 4. Default Seed Credentials (Day 1)

Flyway and `DataInitializer` provision these initial accounts:

| Identity | Role | Email | Password |
| :--- | :--- | :--- | :--- |
| **System Administrator** | `ROLE_ADMIN` | `admin@ledgerline.com` | `Admin@12345` |
| **Chief Auditor** | `ROLE_AUDITOR` | `auditor@ledgerline.com` | `Auditor@12345` |
| **Public Registrations** | `ROLE_ACCOUNT_HOLDER` | Any registered email | Selected by user |

---

## 5. Day 1 Verification Checklist

- [x] **MySQL Database Connected**: Configured with UTF-8 character encoding and HikariCP connection pooling.
- [x] **Flyway Schema Migrations**: `V1__init_schema.sql` and `V2__seed_roles.sql` executed automatically.
- [x] **Three Roles Configured**: `ROLE_ADMIN`, `ROLE_ACCOUNT_HOLDER`, `ROLE_AUDITOR`.
- [x] **Password Hashing**: BCrypt 10 rounds; passwords never stored or logged in plain text.
- [x] **JWT Authentication**: Digitally signed HMAC-SHA256 tokens transmitted via `HttpOnly`, `SameSite=Lax` cookies & bearer headers.
- [x] **Role-Based Authorization**: Protected endpoints (`/api/admin/**`, `/api/accounts/**`, `/api/users/**`) enforced server-side.
- [x] **Zero-Balance Financial Accounts**: Accounts strictly initialized at $0.00; unauthorized balance creation prevented.
- [x] **Append-Only Audit Logging**: All registrations, logins, profile updates, and account creations recorded with IP addresses.
- [x] **React + TypeScript UI**: Modern SaaS interface with emerald/teal design, route guards, and honest empty states.
- [x] **Automated Tests Passing**: 19 automated integration tests covering all Day 1 functional and security requirements.
