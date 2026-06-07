## ADDED Requirements

### Requirement: Keyword Search
The system SHALL support full-text keyword search across goods titles and descriptions.

#### Scenario: Search by keyword
- **WHEN** a user searches with keyword "高等数学"
- **THEN** the system returns paginated goods whose title or description contains "高等数学", sorted by relevance

#### Scenario: Empty keyword search
- **WHEN** a user submits an empty search keyword
- **THEN** the system returns all available goods with default sorting (latest first)

### Requirement: Category Filter
The system SHALL support filtering goods by category.

#### Scenario: Filter by single category
- **WHEN** a user selects category "教材书籍"
- **THEN** the system returns only goods belonging to that category

#### Scenario: Filter with keyword and category
- **WHEN** a user searches "Python" and selects category "教材书籍"
- **THEN** the system returns goods matching both the keyword and the category

### Requirement: Price Range Filter
The system SHALL support filtering goods by price range.

#### Scenario: Filter by min and max price
- **WHEN** a user sets min price to 10 and max price to 50
- **THEN** the system returns only goods with price between 10 and 50 (inclusive)

#### Scenario: Filter by min price only
- **WHEN** a user sets min price to 100
- **THEN** the system returns goods with price >= 100

### Requirement: Condition Filter
The system SHALL support filtering goods by condition level.

#### Scenario: Filter by condition
- **WHEN** a user selects condition "九成新"
- **THEN** the system returns only goods with that condition value

### Requirement: Sorting
The system SHALL support sorting search results by multiple criteria.

#### Scenario: Sort by latest
- **WHEN** a user selects sort "最新发布"
- **THEN** the system returns goods sorted by create_time DESC

#### Scenario: Sort by price ascending
- **WHEN** a user selects sort "价格从低到高"
- **THEN** the system returns goods sorted by price ASC

#### Scenario: Sort by most viewed
- **WHEN** a user selects sort "最多浏览"
- **THEN** the system returns goods sorted by view_count DESC

### Requirement: Search History (Optional Enhancement)
The system MAY store user search history for quick access.

#### Scenario: Save search history
- **WHEN** an authenticated user performs a search
- **THEN** the system saves the search keyword with the user ID for later retrieval
