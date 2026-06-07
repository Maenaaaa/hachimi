## ADDED Requirements

### Requirement: Dashboard Statistics
The system SHALL provide administrators with aggregate platform statistics.

#### Scenario: Get dashboard stats
- **WHEN** an admin requests GET `/api/admin/dashboard`
- **THEN** the system returns: totalUsers, totalGoods, totalOrders, todayActiveUsers, pendingReviewGoods count, pendingReports count

### Requirement: User Management
The system SHALL allow administrators to manage platform users.

#### Scenario: Search users
- **WHEN** an admin searches users by username, nickname, or email
- **THEN** the system returns paginated user list with: id, username, nickname, email, role, status, createTime

#### Scenario: View user detail
- **WHEN** an admin requests GET `/api/admin/user/{id}`
- **THEN** the system returns full user info including: profile, goods count, order count, verification status, report history

#### Scenario: Disable user
- **WHEN** an admin disables a user via PUT `/api/admin/user/{id}/disable`
- **THEN** the system sets user status to "DISABLED", preventing login and new content creation

#### Scenario: Enable user
- **WHEN** an admin re-enables a user via PUT `/api/admin/user/{id}/enable`
- **THEN** the system sets user status to "ACTIVE", restoring full access

### Requirement: Goods Management (Admin)
The system SHALL allow administrators to manage all platform goods.

#### Scenario: List all goods
- **WHEN** an admin requests GET `/api/admin/goods?status=PENDING_REVIEW&page=1&size=20`
- **THEN** the system returns paginated goods list with all statuses including non-public ones

#### Scenario: Force take down goods
- **WHEN** an admin takes down goods via PUT `/api/admin/goods/{id}/take-down` with a reason
- **THEN** the system sets goods status to "TAKEN_DOWN" and notifies the owner

#### Scenario: Delete goods
- **WHEN** an admin deletes goods via DELETE `/api/admin/goods/{id}`
- **THEN** the system logically deletes the goods and notifies the owner

### Requirement: Report Management (Admin)
The system SHALL allow administrators to manage user reports.

#### Scenario: View pending reports
- **WHEN** an admin requests GET `/api/admin/report?status=PENDING`
- **THEN** the system returns paginated reports with reporter and reported entity details

#### Scenario: Handle report
- **WHEN** an admin updates report status and adds handling notes
- **THEN** the system records the handler (admin) ID, handling time, and outcome

### Requirement: Announcement Management
The system SHALL allow administrators to publish and manage platform announcements.

#### Scenario: Create announcement
- **WHEN** an admin creates an announcement with title and content
- **THEN** the system saves the announcement and sends a system notification to all users

#### Scenario: Edit announcement
- **WHEN** an admin edits an existing announcement
- **THEN** the system updates the announcement content

#### Scenario: Delete announcement
- **WHEN** an admin deletes an announcement
- **THEN** the system logically deletes the announcement (it stops showing but notifications already sent remain)

#### Scenario: List announcements
- **WHEN** anyone requests GET `/api/announcement`
- **THEN** the system returns active announcements sorted by create_time DESC
