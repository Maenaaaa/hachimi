## ADDED Requirements

### Requirement: Create Conversation
The system SHALL automatically create or retrieve a conversation when a user initiates chat with another user.

#### Scenario: Start new conversation
- **WHEN** a user sends the first message to another user via a goods context
- **THEN** the system creates a new conversation linking both users and the goods, then sends the message

#### Scenario: Resume existing conversation
- **WHEN** a user sends a message in an existing conversation
- **THEN** the system appends the message to the existing conversation

### Requirement: Send Text Message
The system SHALL support sending text messages via WebSocket.

#### Scenario: Send text message
- **WHEN** a user sends a text message via WebSocket STOMP destination `/app/chat/{conversationId}`
- **THEN** the system persists the message, broadcasts it to the recipient via `/topic/chat/{conversationId}`, and updates the conversation's last message

#### Scenario: Send message to invalid conversation
- **WHEN** a user sends a message to a conversation they are not a participant of
- **THEN** the system returns an error via STOMP `/user/queue/errors`

### Requirement: Send Image Message
The system SHALL support sending image messages via WebSocket.

#### Scenario: Send image message
- **WHEN** a user uploads an image in a chat and sends it
- **THEN** the system uploads the image to MinIO, creates a message with type "IMAGE" and the image URL, and broadcasts to the recipient

### Requirement: Conversation List
The system SHALL provide a list of conversations for the current user, ordered by latest message time.

#### Scenario: Load conversations
- **WHEN** a user requests GET `/api/conversation/list`
- **THEN** the system returns conversations with: other user's nickname/avatar, last message preview, unread count, update time

### Requirement: Message History
The system SHALL support loading paginated message history for a conversation.

#### Scenario: Load message history
- **WHEN** a user requests GET `/api/message/{conversationId}?page=1&size=20`
- **THEN** the system returns messages in chronological order, paginated

### Requirement: Unread Message Count
The system SHALL track and display unread message counts per conversation.

#### Scenario: Get unread counts
- **WHEN** a user requests GET `/api/message/unread-count`
- **THEN** the system returns the total number of unread messages across all conversations

### Requirement: Mark Messages as Read
The system SHALL mark messages as read when the recipient views them.

#### Scenario: Mark conversation as read
- **WHEN** a user opens a conversation (or sends a read receipt)
- **THEN** the system marks all unread messages in that conversation as read and notifies the sender

### Requirement: WebSocket Connection Authentication
The system SHALL authenticate WebSocket connections using JWT token.

#### Scenario: Connect with valid token
- **WHEN** a client connects to WebSocket with a valid JWT token as query parameter
- **THEN** the connection is established and the user is authenticated for STOMP messaging

#### Scenario: Connect without valid token
- **WHEN** a client connects to WebSocket without a valid JWT token
- **THEN** the connection is rejected
