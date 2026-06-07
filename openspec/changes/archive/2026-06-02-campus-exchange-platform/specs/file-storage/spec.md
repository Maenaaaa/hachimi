## ADDED Requirements

### Requirement: Upload Image
The system SHALL support uploading images to MinIO object storage.

#### Scenario: Upload goods image
- **WHEN** a user uploads a JPG/PNG/WebP image (max 5MB) for goods
- **THEN** the system uploads the file to MinIO bucket "goods-images", generates a unique object key, and returns the access URL

#### Scenario: Upload avatar
- **WHEN** a user uploads a JPG/PNG image (max 2MB) as avatar
- **THEN** the system uploads to MinIO bucket "avatars" and returns the access URL

#### Scenario: Upload verification image
- **WHEN** a user uploads a student ID card photo for real-name verification
- **THEN** the system uploads to MinIO bucket "verification" and returns the access URL

#### Scenario: Upload invalid file type
- **WHEN** a user uploads a file that is not JPG/PNG/WebP
- **THEN** the system returns HTTP 400 with message "仅支持 JPG/PNG/WebP 格式的图片"

#### Scenario: Upload oversized file
- **WHEN** a user uploads an image exceeding the size limit
- **THEN** the system returns HTTP 400 with message "图片大小不能超过 X MB"

### Requirement: Get Image URL
The system SHALL provide access URLs for stored images, using pre-signed URLs for private buckets.

#### Scenario: Get public image URL
- **WHEN** the system returns a goods image URL
- **THEN** the URL is either a direct MinIO URL (public bucket) or a pre-signed URL with configurable expiry (private bucket)

### Requirement: Delete Image
The system SHALL allow deletion of uploaded images when associated content is removed.

#### Scenario: Delete goods images
- **WHEN** goods is deleted
- **THEN** the system removes the associated images from MinIO storage

#### Scenario: Delete old avatar on update
- **WHEN** a user uploads a new avatar
- **THEN** the system deletes the old avatar file from MinIO

### Requirement: Chat Image Upload
The system SHALL support uploading images within chat conversations.

#### Scenario: Upload chat image
- **WHEN** a user uploads an image in a chat conversation
- **THEN** the system uploads to MinIO bucket "chat-images" and returns the URL to be sent as a message
