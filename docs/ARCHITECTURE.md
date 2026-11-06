# LedgerLine Architecture & System Design Document (Day 1)

## 1. Executive Summary

**LedgerLine** is an enterprise-grade financial transaction and ledger management system engineered to guarantee financial integrity, strong identity governance, and strict access control. Day 1 establishes the production architecture baseline, role-based security model, database foundations, and full-stack integration between the React frontend and Spring Boot backend.

---

## 2. System Architecture

```
+------------------------------------------------------------------------+
|                          CLIENT PRESENTATION TIER                      |
|                                                                        |
|    React 18 + TypeScript + Vite + Tailwind CSS + Lucide + Recharts    |
|   [Login]    [Register]    [Dashboard]    [Profile]    [Unauthorized] |
+-----------------------------------+------------------------------------+
                                    |
                           HTTPS / REST (JSON)
                       Bearer JWT / HttpOnly Cookie
                                    |
+-----------------------------------v------------------------------------+
|                         BACKEND APPLICATION TIER                       |
|                          (Spring Boot 3.3.5 / Java 21)                 |
|                                                                        |
|  +------------------------------------------------------------------+  |
|  | Security Filter Chain: JwtAuthenticationFilter, BCrypt, RBAC     |  |
|  +------------------------------------------------------------------+  |
|  | REST Controllers: AuthController, UserController, Admin, Account |  |
|  +------------------------------------------------------------------+  |
|  | Service Layer: AuthService, UserService, AccountService, Audit   |  |
|  +------------------------------------------------------------------+  |
|  | Data Layer: Spring Data JPA Repositories + Hibernate ORM         |  |
|  +------------------------------------------------------------------+  |
+-----------------------------------+------------------------------------+
                                    |
                           JDBC Connection Pool
                              (HikariCP 10)
                                    |
+-----------------------------------v------------------------------------+
|                         DATABASE & PERSISTENCE                         |
|                         (MySQL 8.0 / Flyway Migrations)                |
|                                                                        |
|   [users] <-----> [user_roles] <-----> [roles]                         |
|      |                                                                 |
|      +-----------> [account_permissions] <-----> [accounts]            |
|      |                                                                 |
|      +-----------> [audit_logs]                                        |
|      +-----------> [transactions]                                      |
+------------------------------------------------------------------------+
```

---

## 3. Frontend-Backend Communication

1. **Protocol & Serialization**: Standard JSON over HTTP/REST using Axios with central interceptors.
2. **Authentication State**:
   - On successful `POST /api/auth/login`, backend issues a digitally signed JWT with `sub=email`, `userId`, `roles`, and 24-hour expiration.
   - The token is transmitted via an `Authorization: Bearer <token>` header and set in a `SameSite=Lax`, `HttpOnly` cookie.
   - Frontend `AuthContext` hydrates user profile and granted roles from `GET /api/auth/me`.
3. **Error Handling**:
   - Consistent JSON format returned for all errors (`status`, `error`, `message`, `validationErrors`, `timestamp`).

---

## 4. Role-Based Authorization Model

LedgerLine defines three immutable core roles on Day 1:

| Role | Intended Principal | Allowed Capabilities | Restricted Capabilities |
| :--- | :--- | :--- | :--- |
| **`ROLE_ADMIN`** | System Administrator | View all registered users, view all roles, view all accounts, grant account permissions, audit oversight. | Cannot fabricate ledger adjustments without dual control (future phases). |
| **`ROLE_ACCOUNT_HOLDER`** | Customer / Client | Register publicly, view own profile, create accounts (zero balance), view own accounts, update own contact information. | Blocked from `/api/admin/**`, cannot view or manipulate other users' accounts. |
| **`ROLE_AUDITOR`** | Internal / External Compliance | Read-only view of all accounts, transactions, and audit trails. | Blocked from administrative mutation, blocked from creating or modifying accounts/users. |

### Public Registration Rule
Public self-registration strictly assigns `ROLE_ACCOUNT_HOLDER`. Registration requests attempting to inject `ROLE_ADMIN` or other elevated permissions are rejected at the service layer.

---

## 5. Financial Integrity Architecture

1. **Monetary Storage**: Stored as `DECIMAL(19,2)` throughout Hibernate, JPA, and MySQL DDL. Float and double types are prohibited.
2. **Balance Initialization**: On Day 1, all accounts are initialized at `0.00` balance. Arbitrary balance creation via client requests is rejected.
3. **Constraints**: Database-level check `CHECK (balance >= 0.00)` ensures balances cannot silently drop below zero.
4. **Append-Only Auditing**: Every registration, authentication, profile change, and account creation event is logged with actor ID, action name, resource, and client IP address.
