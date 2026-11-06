# LedgerLine Database Schema Documentation (Day 1)

## Entity Relationship Overview

LedgerLine operates an account-centric permission and audit model designed to expand into double-entry bookkeeping in subsequent phases.

```
+---------------+        +----------------------+        +-------------------+
|     USERS     | 1    * |      USER_ROLES      | *    1 |       ROLES       |
+---------------+<-------+----------------------+------->+-------------------+
| id (PK)       |        | user_id (PK, FK)     |        | id (PK)           |
| full_name     |        | role_id (PK, FK)     |        | role_name (UQ)    |
| email (UQ)    |        +----------------------+        | description       |
| password_hash |                                        | created_at        |
| phone_number  |                                        +-------------------+
| enabled       |
| created_at    |
| updated_at    |
+-------+-------+
        | 1
        |
        | *
+-------v--------------+ *    1 +---------------------+
| ACCOUNT_PERMISSIONS  +------->|      ACCOUNTS       |
+----------------------+        +---------------------+
| id (PK)              |        | id (PK)             |
| account_id (FK)      |        | account_number (UQ) |
| user_id (FK)         |        | account_name        |
| can_view             |        | account_type        |
| can_deposit          |        | currency (ISO-4217) |
| can_withdraw         |        | balance DECIMAL     |
| can_transfer         |        | status              |
| granted_by (FK)      |        | created_at          |
| created_at           |        | updated_at          |
+----------------------+        +---------------------+
        |                                  |
        +----------------+                 |
                         |                 |
+-------------------+    |          +------v--------------+
|    AUDIT_LOGS     |    |          |    TRANSACTIONS     |
+-------------------+    |          +---------------------+
| id (PK)           |    |          | id (PK)             |
| user_id (FK)      |    |          | reference_id (UQ)   |
| action            |    |          | transaction_type    |
| resource_type     |    |          | status              |
| resource_id       |    |          | initiated_by (FK)   |
| ip_address        |    |          | created_at          |
| description       |    |          +---------------------+
| created_at        |    |
+-------------------+    |
                         |
```

## Tables & Field Definitions

### 1. `users`
- `id` (BIGINT, PK, AUTO_INCREMENT): Unique user identifier.
- `full_name` (VARCHAR(100), NOT NULL): Legal customer or staff name.
- `email` (VARCHAR(150), NOT NULL, UNIQUE, INDEXED): Primary authentication handle.
- `password_hash` (VARCHAR(255), NOT NULL): BCrypt 10+ rounds hashed password.
- `phone_number` (VARCHAR(30)): E.164 formatted contact phone.
- `enabled` (BOOLEAN, NOT NULL, DEFAULT TRUE): Account active flag for authorization checks.
- `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP).
- `updated_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP).

### 2. `roles`
- `id` (BIGINT, PK, AUTO_INCREMENT): Role ID.
- `role_name` (VARCHAR(50), NOT NULL, UNIQUE): Standard role token (`ROLE_ADMIN`, `ROLE_ACCOUNT_HOLDER`, `ROLE_AUDITOR`).
- `description` (VARCHAR(255)): Human-readable role definition.
- `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP).

### 3. `user_roles`
- `user_id` (BIGINT, NOT NULL, FK -> users.id ON DELETE CASCADE).
- `role_id` (BIGINT, NOT NULL, FK -> roles.id ON DELETE CASCADE).
- Composite PK: `(user_id, role_id)`.

### 4. `accounts`
- `id` (BIGINT, PK, AUTO_INCREMENT): Internal ledger surrogate ID.
- `account_number` (VARCHAR(36), NOT NULL, UNIQUE, INDEXED): Financial identifier format (`LL-XXXXXXXXXX`).
- `account_name` (VARCHAR(100), NOT NULL): Friendly account label.
- `account_type` (VARCHAR(50), NOT NULL): Enum-like string (`CHECKING`, `SAVINGS`, `BUSINESS`, `INVESTMENT`).
- `currency` (VARCHAR(3), NOT NULL, DEFAULT 'USD'): 3-letter ISO-4217 code.
- `balance` (DECIMAL(19,2), NOT NULL, DEFAULT 0.00): High-precision financial balance.
- `status` (VARCHAR(20), NOT NULL, DEFAULT 'ACTIVE'): Lifecycle state (`ACTIVE`, `FROZEN`, `CLOSED`).
- `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP).
- `updated_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP).
- Check Constraint: `CHECK (balance >= 0.00)`.

### 5. `account_permissions`
- `id` (BIGINT, PK, AUTO_INCREMENT).
- `account_id` (BIGINT, NOT NULL, FK -> accounts.id ON DELETE CASCADE).
- `user_id` (BIGINT, NOT NULL, FK -> users.id ON DELETE CASCADE).
- `can_view` (BOOLEAN, NOT NULL, DEFAULT TRUE).
- `can_deposit` (BOOLEAN, NOT NULL, DEFAULT FALSE).
- `can_withdraw` (BOOLEAN, NOT NULL, DEFAULT FALSE).
- `can_transfer` (BOOLEAN, NOT NULL, DEFAULT FALSE).
- `granted_by` (BIGINT, FK -> users.id ON DELETE SET NULL).
- `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP).
- Unique Constraint: `uk_account_user_permission (account_id, user_id)`.

### 6. `audit_logs`
- `id` (BIGINT, PK, AUTO_INCREMENT): Log entry sequence.
- `user_id` (BIGINT, FK -> users.id ON DELETE SET NULL): Acting principal.
- `action` (VARCHAR(100), NOT NULL, INDEXED): Event tag (e.g. `USER_REGISTRATION`, `USER_LOGIN`, `ACCOUNT_CREATION`).
- `resource_type` (VARCHAR(50), NOT NULL): Target entity type (`USER`, `ACCOUNT`, `AUTH`).
- `resource_id` (VARCHAR(100)): Identifier of affected entity.
- `ip_address` (VARCHAR(45)): Client remote IPv4/IPv6 address.
- `description` (VARCHAR(1000)): Event commentary.
- `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP, INDEXED).

### 7. `transactions`
- `id` (BIGINT, PK, AUTO_INCREMENT): Sequence ID.
- `reference_id` (VARCHAR(64), NOT NULL, UNIQUE, INDEXED): Cryptographic or UUID idempotency reference.
- `transaction_type` (VARCHAR(50), NOT NULL): Type category (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER`).
- `status` (VARCHAR(30), NOT NULL, INDEXED): Processing state (`PENDING`, `POSTED`, `REJECTED`, `REVERSED`).
- `initiated_by` (BIGINT, FK -> users.id ON DELETE SET NULL).
- `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP).
