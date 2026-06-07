## ADDED Requirements

### Requirement: Home Page Banner
The system SHALL display a carousel banner on the home page.

#### Scenario: Load banners
- **WHEN** a user visits the home page
- **THEN** the system returns a list of active banners with image URL and optional link

### Requirement: Category Navigation
The system SHALL display category icons for browsing goods by category.

#### Scenario: Load categories
- **WHEN** a user visits the home page
- **THEN** the system returns all active categories with name and icon

### Requirement: Recommended Goods Feed (Waterfall)
The system SHALL display recommended goods in a waterfall/masonry layout on the home page.

#### Scenario: Load recommended goods
- **WHEN** a user visits the home page
- **THEN** the system returns paginated goods sorted by a combination of view count and recency, each item containing: id, title, first image, price, condition, viewCount

#### Scenario: Load more (infinite scroll)
- **WHEN** a user scrolls to the bottom of the recommended goods feed
- **THEN** the system returns the next page of goods or an empty list if no more goods

### Requirement: Latest Goods Feed
The system SHALL display the most recently published goods with infinite scroll.

#### Scenario: Load latest goods
- **WHEN** a user visits the home page or selects "Latest" tab
- **THEN** the system returns paginated goods sorted by create_time DESC, each item containing: id, title, first image, price, condition, viewCount

### Requirement: Goods Detail Page
The system SHALL provide a detailed view of a single goods item.

#### Scenario: View goods detail
- **WHEN** a user requests GET `/api/goods/{id}`
- **THEN** the system returns: all images, title, description, originalPrice, price, condition, campus, viewCount, createTime, seller info (id, nickname, avatar, creditScore)

#### Scenario: View deleted or taken-down goods
- **WHEN** a user requests goods that has been deleted or taken down
- **THEN** the system returns HTTP 404 with message "商品不存在或已下架"

#### Scenario: Seller views own inactive goods
- **WHEN** the seller requests their own inactive goods detail
- **THEN** the system returns the full detail including status
