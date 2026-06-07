## ADDED Requirements

### Requirement: System Notification
The system SHALL send notifications to users for system events.

#### Scenario: System broadcast
- **WHEN** an admin publishes a system announcement
- **THEN** the system creates a notification of type "SYSTEM" for all active users with the announcement content

### Requirement: Order Notification
The system SHALL send notifications for order status changes.

#### Scenario: New order notification
- **WHEN** a buyer creates an order
- **THEN** the system creates a notification of type "ORDER" for the seller with message "您有新的订单待确认"

#### Scenario: Order confirmed notification
- **WHEN** a seller confirms an order
- **THEN** the system creates a notification of type "ORDER" for the buyer with message "您的订单已被卖家确认"

#### Scenario: Order cancelled notification
- **WHEN** a buyer cancels an order
- **THEN** the system creates a notification of type "ORDER" for the seller with message "买家已取消订单"

#### Scenario: Order completed notification
- **WHEN** a buyer marks an order as complete
- **THEN** the system creates a notification of type "ORDER" for the seller with message "订单已完成，请评价买家"

### Requirement: Review Notification
The system SHALL send notifications for review/audit results.

#### Scenario: Goods approved notification
- **WHEN** an admin approves a goods
- **THEN** the system creates a notification of type "REVIEW" for the goods owner with message "您的商品已通过审核"

#### Scenario: Goods rejected notification
- **WHEN** an admin rejects a goods
- **THEN** the system creates a notification of type "REVIEW" with the rejection reason

#### Scenario: Verification result notification
- **WHEN** an admin approves or rejects a real-name verification
- **THEN** the system creates a notification of type "REVIEW" for the applicant

### Requirement: Notification List
The system SHALL allow users to view their notifications.

#### Scenario: Get notifications
- **WHEN** a user requests GET `/api/notification?page=1&size=20`
- **THEN** the system returns paginated notifications sorted by create_time DESC with read status

### Requirement: Mark Notification Read
The system SHALL support marking notifications as read.

#### Scenario: Mark single notification as read
- **WHEN** a user marks a notification as read via PUT `/api/notification/{id}/read`
- **THEN** the system sets is_read to 1

#### Scenario: Mark all as read
- **WHEN** a user marks all notifications as read via PUT `/api/notification/read-all`
- **THEN** the system sets is_read to 1 for all unread notifications of that user

### Requirement: Unread Notification Count
The system SHALL provide the count of unread notifications.

#### Scenario: Get unread count
- **WHEN** a user requests GET `/api/notification/unread-count`
- **THEN** the system returns the number of unread notifications

### Requirement: Real-time Notification Push
The system SHALL push new notifications to online users via WebSocket.

#### Scenario: Push notification to online user
- **WHEN** a notification is created for a user who has an active WebSocket connection
- **THEN** the system pushes the notification via STOMP to `/user/queue/notifications`
