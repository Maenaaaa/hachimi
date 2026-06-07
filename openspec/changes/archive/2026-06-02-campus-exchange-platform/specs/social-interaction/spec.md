## ADDED Requirements

### Requirement: Add Favorite
The system SHALL allow users to add goods to their favorites.

#### Scenario: Add to favorites
- **WHEN** an authenticated user favorites a goods item via POST `/api/favorite`
- **THEN** the system creates a favorite record linking the user and the goods, and the goods' favorite count is incremented

#### Scenario: Duplicate favorite
- **WHEN** a user attempts to favorite goods they already favorited
- **THEN** the system returns HTTP 400 with message "已收藏该商品"

#### Scenario: Favorite own goods
- **WHEN** a user attempts to favorite their own goods
- **THEN** the system returns HTTP 400 with message "不能收藏自己的商品"

### Requirement: Remove Favorite
The system SHALL allow users to remove goods from their favorites.

#### Scenario: Remove favorite
- **WHEN** a user unfavorites a goods via DELETE `/api/favorite/{goodsId}`
- **THEN** the system logically deletes the favorite record and decrements the goods' favorite count

#### Scenario: Remove non-existent favorite
- **WHEN** a user attempts to unfavorite goods they haven't favorited
- **THEN** the system returns HTTP 404

### Requirement: List Favorites
The system SHALL allow users to view their favorited goods.

#### Scenario: View my favorites
- **WHEN** a user requests GET `/api/favorite/my`
- **THEN** the system returns paginated favorited goods with goods' current info (title, first image, price, status)

#### Scenario: Filtered out deleted goods
- **WHEN** a user views favorites and some favorited goods have been deleted
- **THEN** the system excludes deleted goods from the list

### Requirement: Follow User
The system SHALL allow users to follow other users.

#### Scenario: Follow a user
- **WHEN** a user follows another user via POST `/api/follow`
- **THEN** the system creates a follow record and increments both users' follow/follower counts

#### Scenario: Follow self
- **WHEN** a user attempts to follow themselves
- **THEN** the system returns HTTP 400 with message "不能关注自己"

#### Scenario: Duplicate follow
- **WHEN** a user attempts to follow someone they already follow
- **THEN** the system returns HTTP 400 with message "已关注该用户"

### Requirement: Unfollow User
The system SHALL allow users to unfollow other users.

#### Scenario: Unfollow a user
- **WHEN** a user unfollows via DELETE `/api/follow/{userId}`
- **THEN** the system logically deletes the follow record and decrements counts

### Requirement: Check Follow/Favorite Status
The system SHALL allow clients to check whether the current user follows a specific user or has favorited specific goods.

#### Scenario: Check status
- **WHEN** a user requests GET `/api/social/status?userId=X&goodsId=Y`
- **THEN** the system returns `{ "isFollowed": true/false, "isFavorited": true/false }`
