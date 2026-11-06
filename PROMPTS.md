# LedgerLine Development Prompt Log & Audit Trail (PROMPTS.md)

## Entry 1: Day 1 Initialization & Core Architecture Setup
* **Date**: 2026-11-06 10:45:00 UTC+05:30
* **Phase**: Day 1 Project Initialization, Database Architecture, Authentication & Foundation

### 1. Master Request Prompt
```text
Act as a Senior Software Architect, Senior Java Spring Boot Developer, React Developer, MySQL Database Engineer, Security Engineer and QA Automation Engineer.
Your task is to build DAY 1 of a complete financial transaction and ledger management platform named LEDGERLINE.
Day 1 must focus on the application foundation, database architecture, authentication, authorization and initial dashboard.
Create a functional application foundation with working frontend, backend, MySQL database connection, Flyway migrations, authentication, role-based access control and 14 automated tests.
```

### 2. Implementation Summary
- Initialized Git repository with clean multi-module structure (`frontend/`, `backend/`, `database/`, `docs/`).
- Configured Spring Boot 3.3.5 with Java 21/23, Spring Security 6, Spring Data JPA, Hibernate, Flyway, and JJWT 0.12.6.
- Established Flyway migrations (`V1__init_schema.sql`, `V2__seed_roles.sql`) with tables for `users`, `roles`, `user_roles`, `accounts`, `account_permissions`, `audit_logs`, `transactions`.
- Enforced monetary integrity with `DECIMAL(19,2)` and `balance = 0.00` initial constraints.
- Built layered architecture: JPA Entities, Spring Data Repositories, DTOs, Mappers, Exception Handlers, Security Filters, and REST Controllers.
- Implemented comprehensive automated test suite testing all 14 acceptance criteria.

### 3. Problems Discovered During Compilation & Testing
1. **Lombok Annotation Processor on JDK 23**:
   - *Problem*: Maven compilation failed with "cannot find symbol: method builder(), method getEmail(), variable log". In JDK 23, Lombok annotations are not automatically discovered without explicit compiler plugin configuration.
   - *Correction*: Added `maven-compiler-plugin` 3.13.0 with `annotationProcessorPaths` targeting `org.projectlombok:lombok:1.18.34` in `pom.xml`.
2. **Transactional Boundary Conflict with Audit Logging**:
   - *Problem*: In `AuthenticationIntegrationTest`, registration returned HTTP 500 (`UnexpectedRollbackException`) caused by `Referential integrity constraint violation: "fk_audit_logs_user"`.
   - *Root Cause*: `AuditLogService.log()` had `@Transactional(propagation = Propagation.REQUIRES_NEW)`. Because the parent user creation transaction in `AuthService.register()` had not yet committed, the new transaction could not resolve the uncommitted `user_id` foreign key.
   - *Correction*: Modified `AuditLogService.log()` to use standard `@Transactional` (joining the parent transaction), ensuring atomic consistency and valid foreign keys without transaction deadlock or rollback.

### 4. Git Commits & Branch Traceability
- Initialized repository on `main`.
- Feature branches created:
  - `feature/project-setup`
  - `feature/database-design`
  - `feature/authentication`
  - `feature/security`
  - `feature/frontend-foundation`
