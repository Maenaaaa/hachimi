## ADDED Requirements

### Requirement: Publish Goods
The system SHALL allow authenticated users to publish goods with multi-step form data.

#### Scenario: Successful goods publication
- **WHEN** a user completes all steps (basic info, images, description) and submits
- **THEN** the system creates a goods record with status "PENDING_REVIEW", stores images in MinIO, and returns the goods ID

#### Scenario: Publish without images
- **WHEN** a user submits goods without uploading any images
- **THEN** the system returns HTTP 400 with message "请至少上传一张商品图片"

#### Scenario: Publish with missing required fields
- **WHEN** a user submits goods without title or category
- **THEN** the system returns HTTP 400 with field-level validation errors

### Requirement: Edit Goods
The system SHALL allow goods owners to edit their goods information.

#### Scenario: Edit goods details
- **WHEN** the goods owner submits updated title, description, price, or images
- **THEN** the system updates the goods record and resets status to "PENDING_REVIEW" if the goods was previously approved

#### Scenario: Non-owner edit attempt
- **WHEN** a user attempts to edit goods they do not own
- **THEN** the system returns HTTP 403

### Requirement: Delete Goods
The system SHALL allow goods owners to logically delete their goods.

#### Scenario: Soft delete goods
- **WHEN** the goods owner requests deletion of their goods
- **THEN** the system sets `deleted` flag to 1 and the goods no longer appears in any listing

#### Scenario: Delete goods with active order
- **WHEN** a user attempts to delete goods that has an active (non-completed, non-cancelled) order
- **THEN** the system returns HTTP 400 with message "该商品有进行中的订单，无法删除"

### Requirement: Goods Review (Admin)
The system SHALL allow administrators to review and approve/reject goods.

#### Scenario: Admin approves goods
- **WHEN** an admin approves a "PENDING_REVIEW" goods
- **THEN** the system sets status to "ACTIVE" (listed) and sends notification to the owner

#### Scenario: Admin rejects goods
- **WHEN** an admin rejects goods with a reason
- **THEN** the system sets status to "REJECTED" and sends notification with the rejection reason

### Requirement: Toggle Goods Status
The system SHALL allow goods owners and admins to toggle goods listing status (on/off shelf).

#### Scenario: Owner takes goods off shelf
- **WHEN** the goods owner sets status to "INACTIVE"
- **THEN** the goods is hidden from browsing but retained in the owner's goods list

#### Scenario: Admin forces goods off shelf
- **WHEN** an admin forces a goods off shelf
- **THEN** the goods is hidden and a violation notice is sent to the owner

### Requirement: Record Goods View
The system SHALL record each view of a goods detail page.

#### Scenario: Record view
- **WHEN** a user opens a goods detail page
- **THEN** the system increments the view count and creates a view record with viewer ID (if logged in) and timestamp
