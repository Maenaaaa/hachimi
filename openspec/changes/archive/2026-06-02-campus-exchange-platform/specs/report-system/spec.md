## ADDED Requirements

### Requirement: Report Goods
The system SHALL allow users to report inappropriate goods.

#### Scenario: Report goods with reason
- **WHEN** a user submits a report with goodsId, reason category (虚假信息/欺诈行为/广告内容/违规内容), and optional description
- **THEN** the system creates a report record with type "GOODS" and status "PENDING"

#### Scenario: Report own goods
- **WHEN** a user attempts to report their own goods
- **THEN** the system returns HTTP 400 with message "不能举报自己的商品"

#### Scenario: Duplicate report
- **WHEN** a user attempts to report the same goods again while a previous report is still pending
- **THEN** the system returns HTTP 400 with message "您已举报过该商品，请等待处理"

### Requirement: Report User
The system SHALL allow users to report other users.

#### Scenario: Report user with reason
- **WHEN** a user submits a report with target userId, reason category, and optional description
- **THEN** the system creates a report record with type "USER" and status "PENDING"

### Requirement: Admin Handle Report
The system SHALL allow administrators to review and handle reports.

#### Scenario: Admin approves report
- **WHEN** an admin sets a report status to "APPROVED" with a handling note and optional penalty (warn user, take down goods, ban user)
- **THEN** the system updates the report, applies the penalty if specified, and notifies the reporter and the reported party

#### Scenario: Admin rejects report
- **WHEN** an admin sets a report status to "REJECTED" with a note explaining why
- **THEN** the system updates the report and notifies the reporter

### Requirement: Report List (Admin)
The system SHALL provide administrators with a filterable list of all reports.

#### Scenario: View reports by status
- **WHEN** an admin requests GET `/api/admin/report?status=PENDING&page=1&size=20`
- **THEN** the system returns paginated reports with reporter info, reported goods/user info, reason, status

### Requirement: Report List (User)
The system SHALL allow users to view the status of reports they have submitted.

#### Scenario: View my reports
- **WHEN** a user requests GET `/api/report/my`
- **THEN** the system returns paginated reports submitted by the user with current status
