# LedgerLine Database Architecture & Migrations

This directory contains the database design specifications, data dictionary, and migration guidelines for the **LedgerLine** financial transaction and ledger platform.

## Overview

The database is powered by **MySQL 8.0** with **Flyway** migration management. The primary objectives are:
1. **Financial Integrity**: Monetary values are stored strictly as `DECIMAL(19,2)`, preventing floating-point rounding errors.
2. **Auditability**: An append-only `audit_logs` table logs critical operational actions with timestamps and client IP addresses.
3. **Strict Constraints**: Foreign keys with cascading rules, composite keys on role joins, unique constraints on account numbers and emails, and `CHECK (balance >= 0.00)` constraints protect data consistency at the engine layer.

## Flyway Migration Structure

Migrations reside in `backend/src/main/resources/db/migration/`:
- `V1__init_schema.sql`: Core DDL for `users`, `roles`, `user_roles`, `accounts`, `account_permissions`, `audit_logs`, and `transactions`.
- `V2__seed_roles.sql`: Initial seed data for system roles (`ROLE_ADMIN`, `ROLE_ACCOUNT_HOLDER`, `ROLE_AUDITOR`).

## Running with Docker Compose

A ready-to-run MySQL 8 container is configured in the root directory:

```powershell
docker compose up -d
```

Connection parameters:
- **Host**: `localhost`
- **Port**: `3306`
- **Database**: `ledgerline_db`
- **User**: `ledger_user`
- **Password**: `ledger_secure_password_replace_me`
