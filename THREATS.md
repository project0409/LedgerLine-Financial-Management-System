# LedgerLine Threat Model & Security Assessment (Day 1)

## 1. Security Philosophy & Defense-in-Depth

In financial transaction and ledger systems, security cannot be retrofitted; it must be ingrained in the architectural foundation. The Day 1 implementation of LedgerLine adheres to a defense-in-depth model where every security assertion is enforced server-side within the Spring Security filter chain and domain service transactions.

---

## 2. Threat Analysis & Mitigations

### 2.1 Unauthorized Account Access
* **Threat**: An attacker or rogue authenticated user attempts to access account details or balances belonging to another organization or customer (`GET /api/accounts/{id}`).
* **Attack Scenario**: Parameter tampering by substituting another user's `accountId` in REST endpoints.
* **Mitigation (Day 1)**:
  - Account visibility is gated by explicit records in `account_permissions`.
  - In `AccountService.getAccountById()`, the authenticated principal is inspected. Unless the user holds `ROLE_ADMIN` or `ROLE_AUDITOR`, a relational lookup checks `accountPermissionRepository.findByAccountIdAndUserId(accountId, userId)`. If `canView == false` or no record exists, an `AccessDeniedException` (HTTP 403 Forbidden) is thrown immediately.
* **Remaining Risk**: Shared credentials between organizational actors. Mitigation in future phases: Multi-Factor Authentication (MFA) and device fingerprinting.

### 2.2 Password Theft & Credential Compromise
* **Threat**: Offline database compromise, credential dumps, or sniffing leading to recovered user passwords.
* **Attack Scenario**: Attacker obtains database snapshot and attempts dictionary or rainbow table attacks against stored hashes.
* **Mitigation (Day 1)**:
  - Plaintext passwords are never persisted or returned in API responses.
  - Spring Security `BCryptPasswordEncoder` is utilized with salt generation per credential and adaptive work factor (10+ rounds).
* **Remaining Risk**: Users choosing weak passwords. Mitigation in future phases: Integration with HaveIBeenPwned API for breached password checks and mandatory complexity rules.

### 2.3 JWT Misuse & Token Theft
* **Threat**: Interception of JSON Web Tokens via Cross-Site Scripting (XSS) or network eavesdropping.
* **Attack Scenario**: Malicious script reads tokens stored in browser `localStorage` and exfiltrates them to an attacker-controlled endpoint.
* **Mitigation (Day 1)**:
  - Tokens are transmitted via `HttpOnly`, `SameSite=Lax` cookies, preventing JavaScript access in the DOM.
  - Secret keys are at least 256 bits (HMAC-SHA256) loaded from environment variables (`JWT_SECRET`) and never hardcoded in production binaries.
  - Tokens carry strict expiration (`exp`) claims (default 24h, configurable).
* **Remaining Risk**: Stolen valid tokens remain active until expiration. Mitigation in future phases: Redis-backed token revocation / denylist and refresh token rotation.

### 2.4 Privilege Escalation During Public Registration
* **Threat**: An adversary registers a new account and injects `"roles": ["ROLE_ADMIN"]` into the JSON payload.
* **Attack Scenario**: Mass assignment vulnerability giving administrative control to unvetted users.
* **Mitigation (Day 1)**:
  - `RegisterRequest` DTO strictly defines only registration fields (`fullName`, `email`, `password`, `confirmPassword`, `phoneNumber`).
  - `AuthService.register()` explicitly ignores client-supplied roles and fetches `ROLE_ACCOUNT_HOLDER` from the database.
  - Administrative endpoints (`/api/admin/**`) are protected with `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`.
* **Remaining Risk**: Compromise of administrator credentials. Mitigation in future phases: Dual-authorization (maker-checker) for role promotion.

### 2.5 SQL Injection
* **Threat**: Malicious SQL fragments passed in query parameters or payload bodies to alter database logic.
* **Attack Scenario**: Injecting `' OR 1=1 --` into login email or account queries.
* **Mitigation (Day 1)**:
  - Zero dynamic SQL string concatenation.
  - Spring Data JPA and Hibernate ORM utilize parameterized queries (`PreparedStatement`) across all repositories.
  - Email queries utilize `findByEmailIgnoreCase(String email)` which safely binds parameters.
* **Remaining Risk**: Complex native queries in future phases if improperly constructed. Mitigation: Mandatory peer review and static analysis for any `@Query(nativeQuery = true)`.

### 2.6 Sensitive Data Exposure
* **Threat**: Leaking password hashes, internal server error stack traces, or customer PII in responses or public logs.
* **Attack Scenario**: Inducing a 500 error to view system stack traces and database credentials.
* **Mitigation (Day 1)**:
  - Jackson serialization ignores sensitive fields via dedicated DTOs (`UserResponse`, `AccountResponse`). Entities are never returned directly from controllers.
  - `GlobalExceptionHandler` intercepts unhandled exceptions and returns a generic sanitized JSON response (`"An unexpected error occurred. Please contact system support."`).
* **Remaining Risk**: Logging sensitive information in debug logs. Mitigation: Custom log scrubbing for card/account numbers.

### 2.7 Unauthorized Money / Balance Creation
* **Threat**: A client supplies an initial balance of $1,000,000 when creating a new account.
* **Attack Scenario**: Fabricating money by altering `POST /api/accounts` payload.
* **Mitigation (Day 1)**:
  - `CreateAccountRequest` does not permit the client to specify a balance.
  - `AccountService.createAccount()` strictly hardcodes `balance = BigDecimal.ZERO.setScale(2)`.
  - Database check constraint `CHECK (balance >= 0.00)` prevents negative values.
* **Remaining Risk**: Internal transfers in future phases. Mitigation: Double-entry ledger architecture ensuring debits equal credits in atomic transactions.

### 2.8 Audit Log Tampering
* **Threat**: An insider or attacker modifies or deletes audit logs to cover unauthorized activity.
* **Mitigation (Day 1)**:
  - `AuditLogRepository` only implements read and insert operations; no update or delete endpoints exist.
  - Database migrations omit update triggers for `audit_logs`.
* **Remaining Risk**: Direct DB access by a database administrator. Mitigation in future phases: Cryptographic hash chains (Merkle tree) across audit rows.
