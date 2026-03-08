-- BR-3.1: Workflow-based file approval
CREATE TABLE file_transfer_request (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    requester VARCHAR(255) NOT NULL,
    recipient VARCHAR(255),
    room_id VARCHAR(255),
    file_ref VARCHAR(512),
    content_text TEXT,
    status VARCHAR(32) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    decided_at TIMESTAMP NULL,
    approver VARCHAR(255)
);
