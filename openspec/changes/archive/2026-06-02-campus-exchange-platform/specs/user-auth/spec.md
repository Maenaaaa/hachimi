## ADDED Requirements

### Requirement: User Registration
The system SHALL allow new users to register with a unique username and password.

#### Scenario: Successful registration
- **WHEN** a visitor submits a registration form with a unique username, valid password (6-20 characters), and required profile fields
- **THEN** the system creates a new user account with role "USER" and returns a JWT token pair (access + refresh)

#### Scenario: Duplicate username
- **WHEN** a visitor submits a registration form with an already-registered username
- **THEN** the system returns HTTP 400 with message "用户名已存在"

#### Scenario: Invalid registration data
- **WHEN** a visitor submits a registration form with missing required fields or password shorter than 6 characters
- **THEN** the system returns HTTP 400 with field-level validation errors

### Requirement: User Login
The system SHALL authenticate users by username and password, issuing JWT tokens.

#### Scenario: Successful login
- **WHEN** a user submits correct username and password credentials
- **THEN** the system returns an access token (24h expiry) and a refresh token (7d expiry)

#### Scenario: Failed login
- **WHEN** a user submits incorrect username or password
- **THEN** the system returns HTTP 401 with message "用户名或密码错误"

#### Scenario: Login with disabled account
- **WHEN** a user whose account has been disabled attempts to login
- **THEN** the system returns HTTP 403 with message "账号已被禁用"

### Requirement: JWT Token Refresh
The system SHALL support refreshing an expired access token using a valid refresh token.

#### Scenario: Successful token refresh
- **WHEN** a client submits a valid refresh token to the refresh endpoint
- **THEN** the system returns a new access token and refresh token pair

#### Scenario: Expired or invalid refresh token
- **WHEN** a client submits an expired or tampered refresh token
- **THEN** the system returns HTTP 401 and the client must re-login

### Requirement: Role-Based Access Control
The system SHALL enforce role-based access: "USER" for normal users, "ADMIN" for administrators.

#### Scenario: User accesses user endpoint
- **WHEN** a user with role "USER" accesses a `/api/user/**` endpoint
- **THEN** the request is authorized

#### Scenario: User accesses admin endpoint
- **WHEN** a user with role "USER" accesses an `/api/admin/**` endpoint
- **THEN** the system returns HTTP 403

#### Scenario: Admin accesses admin endpoint
- **WHEN** a user with role "ADMIN" accesses an `/api/admin/**` endpoint
- **THEN** the request is authorized

### Requirement: Logout
The system SHALL support logout by invalidating the refresh token.

#### Scenario: Successful logout
- **WHEN** an authenticated user calls the logout endpoint with a valid refresh token
- **THEN** the system invalidates the refresh token and returns success
