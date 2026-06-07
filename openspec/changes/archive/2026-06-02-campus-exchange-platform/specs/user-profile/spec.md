## ADDED Requirements

### Requirement: View Profile
The system SHALL allow authenticated users to view their own profile and other users' public profiles.

#### Scenario: View own profile
- **WHEN** an authenticated user requests their own profile via GET `/api/user/profile`
- **THEN** the system returns full profile data including username, nickname, avatar, phone, email, school, realName, creditScore

#### Scenario: View other user's public profile
- **WHEN** any user requests another user's public profile via GET `/api/user/profile/{id}`
- **THEN** the system returns public profile fields only: nickname, avatar, school, creditScore, goodsCount, reviewCount

### Requirement: Edit Profile
The system SHALL allow users to update their profile information.

#### Scenario: Update nickname and school
- **WHEN** a user submits updated nickname and school fields
- **THEN** the system updates the profile and returns the updated user information

#### Scenario: Update with invalid data
- **WHEN** a user submits an empty nickname or excessively long school name
- **THEN** the system returns HTTP 400 with validation error details

### Requirement: Change Password
The system SHALL allow users to change their password by providing the old password.

#### Scenario: Successful password change
- **WHEN** a user submits correct old password and a new valid password (6-20 chars)
- **THEN** the system updates the password hash and invalidates all existing tokens

#### Scenario: Incorrect old password
- **WHEN** a user submits an incorrect old password
- **THEN** the system returns HTTP 400 with message "原密码错误"

### Requirement: Upload Avatar
The system SHALL allow users to upload a profile avatar image.

#### Scenario: Upload valid avatar
- **WHEN** a user uploads a JPG/PNG image under 2MB as avatar
- **THEN** the system stores the image in MinIO, updates the user's avatar URL, and returns the new URL

#### Scenario: Upload invalid file
- **WHEN** a user uploads a non-image file or file exceeding 2MB
- **THEN** the system returns HTTP 400 with appropriate error message

### Requirement: Real-Name Verification
The system SHALL allow users to submit real-name verification with their real name, student ID, and student ID card photo.

#### Scenario: Submit verification
- **WHEN** a user submits real name, student ID, and uploads a student ID card photo
- **THEN** the system creates a verification record with status "PENDING" and notifies admins

#### Scenario: Admin approves verification
- **WHEN** an admin approves a pending verification record
- **THEN** the system updates the user's realName field, sets verification status to "APPROVED", and sends a notification to the user

#### Scenario: Admin rejects verification
- **WHEN** an admin rejects a verification record with a reason
- **THEN** the system sets verification status to "REJECTED" and sends a notification with the rejection reason

#### Scenario: Duplicate verification
- **WHEN** a user who already has a pending verification submits again
- **THEN** the system returns HTTP 400 with message "已有待审核的实名认证申请"
