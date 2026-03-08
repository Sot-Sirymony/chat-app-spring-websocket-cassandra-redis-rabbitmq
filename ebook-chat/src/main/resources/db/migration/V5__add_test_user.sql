-- Non-admin test user (ROLE_USER only): username 'user', password 'password'
-- BCrypt hash below is for 'password' (strength 10)
INSERT INTO ebook_chat.user (username, email, name, password) VALUES
  ('user', 'user@example.com', 'Test User', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG');
INSERT INTO ebook_chat.user_role (username, role_id) VALUES ('user', 2);
