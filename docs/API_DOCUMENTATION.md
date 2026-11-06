# LedgerLine REST API Documentation (Day 1)

**Base URL**: `http://localhost:8080/api`  
**Swagger UI**: `http://localhost:8080/swagger-ui.html`  
**OpenAPI Specification**: `http://localhost:8080/v3/api-docs`

---

## Response Envelope Structure

All REST endpoints return a uniform response envelope:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-11-06T10:45:00"
}
```

Error responses:

```json
{
  "success": false,
  "status": 400,
  "error": "Bad Request",
  "message": "Specific error message",
  "path": "/api/...",
  "timestamp": "2026-11-06T10:45:00",
  "validationErrors": {
    "field": "Validation message"
  }
}
```

---

## 1. Authentication Endpoints

### 1.1 Register User
- **Method**: `POST`
- **Path**: `/api/auth/register`
- **Access**: Public
- **Request Body**:
```json
{
  "fullName": "Jane Doe",
  "email": "jane.doe@example.com",
  "phoneNumber": "+1-555-0155",
  "password": "SecurePassword123!",
  "confirmPassword": "SecurePassword123!"
}
```
- **Response**: `201 Created` with created user details (role is assigned `ROLE_ACCOUNT_HOLDER`).

### 1.2 Login
- **Method**: `POST`
- **Path**: `/api/auth/login`
- **Access**: Public
- **Request Body**:
```json
{
  "email": "jane.doe@example.com",
  "password": "SecurePassword123!"
}
```
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Authentication successful",
  "data": {
    "token": "eyJhbGciOi...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "fullName": "Jane Doe",
      "email": "jane.doe@example.com",
      "phoneNumber": "+1-555-0155",
      "enabled": true,
      "roles": ["ROLE_ACCOUNT_HOLDER"]
    }
  }
}
```
*Note: Sets an `HttpOnly`, `SameSite=Lax` cookie named `ledgerline_jwt` in addition to returning the Bearer token.*

### 1.3 Logout
- **Method**: `POST`
- **Path**: `/api/auth/logout`
- **Access**: Authenticated
- **Response**: `200 OK` (clears cookie).

### 1.4 Get Current Authenticated User (Session Verification)
- **Method**: `GET`
- **Path**: `/api/auth/me`
- **Access**: Authenticated
- **Response**: `200 OK` with user details and roles.

---

## 2. User Management Endpoints

### 2.1 Get Current User Profile
- **Method**: `GET`
- **Path**: `/api/users/me`
- **Access**: Authenticated (`ROLE_ADMIN`, `ROLE_ACCOUNT_HOLDER`, `ROLE_AUDITOR`)
- **Response**: `200 OK`

### 2.2 Update Profile
- **Method**: `PUT`
- **Path**: `/api/users/me`
- **Access**: Authenticated
- **Request Body**:
```json
{
  "fullName": "Jane Doe Smith",
  "phoneNumber": "+1-555-0199"
}
```
- **Response**: `200 OK`

---

## 3. Administrative Governance Endpoints

### 3.1 List All Users
- **Method**: `GET`
- **Path**: `/api/admin/users`
- **Access**: Admin Only (`ROLE_ADMIN`)
- **Response**: `200 OK` (List of user objects).

### 3.2 List System Roles
- **Method**: `GET`
- **Path**: `/api/admin/roles`
- **Access**: Admin Only (`ROLE_ADMIN`)
- **Response**: `200 OK` (List of role objects).

---

## 4. Account Foundation Endpoints

### 4.1 Create Account
- **Method**: `POST`
- **Path**: `/api/accounts`
- **Access**: Authenticated
- **Request Body**:
```json
{
  "accountName": "Primary Operating",
  "accountType": "CHECKING",
  "currency": "USD"
}
```
- **Response**: `201 Created` with `balance: 0.00` and generated `accountNumber: "LL-..."`.

### 4.2 List Permitted Accounts
- **Method**: `GET`
- **Path**: `/api/accounts`
- **Access**: Authenticated
- **Behavior**:
  - `ROLE_ADMIN` & `ROLE_AUDITOR`: View all system accounts.
  - `ROLE_ACCOUNT_HOLDER`: View only accounts where the user holds `can_view=true`.
- **Response**: `200 OK`

### 4.3 Get Account By ID
- **Method**: `GET`
- **Path**: `/api/accounts/{id}`
- **Access**: Authenticated (checked against permissions).
- **Response**: `200 OK` (or `403 Forbidden` if unauthorized).
