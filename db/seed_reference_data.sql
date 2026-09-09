INSERT INTO roles (name)
VALUES ('ADMIN'), ('ANALYST'), ('MERCHANT')
ON CONFLICT (name) DO NOTHING;

INSERT INTO card_schemes (code, name)
VALUES
    ('VISA', 'Visa'),
    ('MASTERCARD', 'Mastercard')
ON CONFLICT (code) DO NOTHING;
