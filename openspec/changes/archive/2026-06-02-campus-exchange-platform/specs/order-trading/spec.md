## ADDED Requirements

### Requirement: Create Order
The system SHALL allow a buyer to create an order for a goods item.

#### Scenario: Successful order creation
- **WHEN** a buyer submits POST `/api/order` with goodsId and optional remark
- **THEN** the system creates an order with status "PENDING", links to the goods and both users, and sends a notification to the seller

#### Scenario: Order own goods
- **WHEN** a user attempts to order their own goods
- **THEN** the system returns HTTP 400 with message "不能购买自己的商品"

#### Scenario: Order unavailable goods
- **WHEN** a user attempts to order goods that are not in "ACTIVE" status
- **THEN** the system returns HTTP 400 with message "商品已下架或售出"

### Requirement: Seller Confirm Order
The system SHALL allow the seller to confirm an order, changing status to "IN_PROGRESS".

#### Scenario: Seller confirms
- **WHEN** the seller of the goods confirms a "PENDING" order
- **THEN** the system updates order status to "IN_PROGRESS", records the action in order_log, and notifies the buyer

#### Scenario: Non-seller confirmation attempt
- **WHEN** a user who is not the seller attempts to confirm an order
- **THEN** the system returns HTTP 403

### Requirement: Buyer Cancel Order
The system SHALL allow the buyer to cancel an order before the seller confirms.

#### Scenario: Buyer cancels pending order
- **WHEN** the buyer cancels a "PENDING" order
- **THEN** the system updates status to "CANCELLED", records in order_log, and notifies the seller

#### Scenario: Cancel confirmed order
- **WHEN** a buyer attempts to cancel an order already confirmed by the seller (status "IN_PROGRESS")
- **THEN** the system returns HTTP 400 with message "订单已确认，无法取消，请联系卖家"

### Requirement: Complete Order
The system SHALL allow the buyer to mark an order as completed.

#### Scenario: Buyer completes order
- **WHEN** the buyer marks an "IN_PROGRESS" order as completed
- **THEN** the system updates status to "COMPLETED", records in order_log, and prompts both parties to leave a review

### Requirement: Order List
The system SHALL allow users to view their orders as buyer or seller.

#### Scenario: View orders as buyer
- **WHEN** a user requests GET `/api/order/buyer?status=PENDING`
- **THEN** the system returns paginated orders where the user is buyer, filtered by status

#### Scenario: View orders as seller
- **WHEN** a user requests GET `/api/order/seller`
- **THEN** the system returns paginated orders where the user is seller

### Requirement: Order Detail
The system SHALL provide detailed order information.

#### Scenario: View order detail
- **WHEN** a participant requests GET `/api/order/{id}`
- **THEN** the system returns order details including goods info, both users' basic info, order log, and status

#### Scenario: Non-participant view attempt
- **WHEN** a user who is neither buyer nor seller requests the order detail
- **THEN** the system returns HTTP 403

### Requirement: Order State Machine
The system SHALL enforce valid order state transitions only.

#### Scenario: Valid transitions
- **WHEN** any order state change is requested
- **THEN** the system only allows: PENDING → IN_PROGRESS (seller confirm), PENDING → CANCELLED (buyer cancel), IN_PROGRESS → COMPLETED (buyer complete)

#### Scenario: Invalid transition
- **WHEN** an invalid transition is attempted (e.g., COMPLETED → CANCELLED)
- **THEN** the system returns HTTP 400 with message "无效的订单状态变更"
