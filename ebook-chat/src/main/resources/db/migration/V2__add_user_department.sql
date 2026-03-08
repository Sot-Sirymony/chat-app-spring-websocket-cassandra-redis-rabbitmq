-- BR-2.1: Add department to User for ABAC
ALTER TABLE user ADD COLUMN department VARCHAR(255) NULL;
