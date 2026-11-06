-- ==============================================================================
-- Flyway Migration V2: Seed Essential Roles
-- ==============================================================================

INSERT INTO roles (role_name, description, created_at)
VALUES 
    ('ROLE_ADMIN', 'System Administrator with user management and governance permissions', CURRENT_TIMESTAMP),
    ('ROLE_ACCOUNT_HOLDER', 'Account Holder with access to own accounts and authorized operations', CURRENT_TIMESTAMP),
    ('ROLE_AUDITOR', 'Auditor with read-only permissions across ledger and audit logs', CURRENT_TIMESTAMP);
