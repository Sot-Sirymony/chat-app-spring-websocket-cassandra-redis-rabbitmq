-- BR-3.3: File metadata for MinIO-stored files (object id = id)
CREATE TABLE file_metadata (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    uploader VARCHAR(255) NOT NULL,
    filename VARCHAR(512),
    content_type VARCHAR(255),
    size_bytes BIGINT,
    request_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL
);
