-- Ensure permissions
INSERT INTO permissions(code, name) VALUES
  ('ORDER_READ', 'Read orders'),
  ('ORDER_CREATE', 'Create orders'),
  ('ORDER_WRITE', 'Write orders')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Ensure roles
INSERT INTO roles(code, name) VALUES
  ('ROLE_ADMIN', 'Admin'),
  ('ROLE_USER', 'User')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Map role-permissions (idempotent)
INSERT IGNORE INTO role_permissions(role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p
WHERE r.code = 'ROLE_ADMIN';

INSERT IGNORE INTO role_permissions(role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p
WHERE r.code = 'ROLE_USER' AND p.code IN ('ORDER_READ', 'ORDER_CREATE');

-- Seed/refresh admin account with bcrypt('admin123')
INSERT INTO users(username, password, enabled) VALUES
  ('admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5/5r8Fsu5RAwOB1p5MNDoAuCEZRlm.K', 1)
ON DUPLICATE KEY UPDATE password = VALUES(password), enabled = 1;

-- Bind admin to ROLE_ADMIN
INSERT IGNORE INTO user_roles(user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON u.username = 'admin' AND r.code = 'ROLE_ADMIN';
