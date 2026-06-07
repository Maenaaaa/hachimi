## ADDED Requirements

### Requirement: Create Review
The system SHALL allow users to review each other after a completed order.

#### Scenario: Buyer reviews seller
- **WHEN** a buyer submits a review with rating (1-5) and optional text for a completed order where they haven't reviewed the seller yet
- **THEN** the system creates a review record, updates the seller's average credit score, and notifies the seller

#### Scenario: Seller reviews buyer
- **WHEN** a seller submits a review with rating (1-5) and optional text for a completed order where they haven't reviewed the buyer yet
- **THEN** the system creates a review record, updates the buyer's average credit score, and notifies the buyer

#### Scenario: Duplicate review
- **WHEN** a user attempts to review the same party twice for the same order
- **THEN** the system returns HTTP 400 with message "已评价过该用户"

#### Scenario: Review without completed order
- **WHEN** a user attempts to review another user without a completed order between them
- **THEN** the system returns HTTP 400

### Requirement: View Reviews
The system SHALL allow users to view reviews for a specific user.

#### Scenario: View user reviews
- **WHEN** any user requests GET `/api/review/user/{userId}?page=1&size=10`
- **THEN** the system returns paginated reviews with: reviewer nickname/avatar, rating, content, createTime

#### Scenario: View my received reviews
- **WHEN** an authenticated user requests GET `/api/review/received`
- **THEN** the system returns paginated reviews where the user is the reviewee

### Requirement: Rating Calculation
The system SHALL calculate and display a user's credit score as the average of all ratings received.

#### Scenario: Credit score update
- **WHEN** a new review is created for a user
- **THEN** the system recalculates the user's creditScore as the average of all non-deleted ratings, rounded to 1 decimal place
