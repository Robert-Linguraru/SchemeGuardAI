-- Development-only administrator account.
-- Login email: admin@schemeguard.local
-- Password: admin123

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO roles (name)
VALUES ('ADMIN')
ON CONFLICT (name) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, status)
VALUES (
    'admin@schemeguard.local',
    crypt('admin123', gen_salt('bf', 10)),
    'admin',
    'ACTIVE'
)
ON CONFLICT (email) DO UPDATE
SET password_hash = EXCLUDED.password_hash,
    full_name = EXCLUDED.full_name,
    status = 'ACTIVE',
    updated_at = now();

INSERT INTO user_roles (user_id, role_id)
SELECT users.id, roles.id
FROM users
JOIN roles ON roles.name = 'ADMIN'
WHERE users.email = 'admin@schemeguard.local'
ON CONFLICT (user_id, role_id) DO NOTHING;
