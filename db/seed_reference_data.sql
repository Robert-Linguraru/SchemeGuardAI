INSERT INTO roles (name)
VALUES ('ADMIN'), ('ANALYST'), ('MERCHANT')
ON CONFLICT (name) DO NOTHING;
INSERT INTO card_schemes (code, name)
VALUES
    ('VISA', 'Visa'),
    ('MASTERCARD', 'Mastercard')
ON CONFLICT (code) DO NOTHING;
INSERT INTO country_regions (merchant_country, region)
VALUES
    ('DE', 'EU'),
    ('FR', 'EU'),
    ('IT', 'EU'),
    ('ES', 'EU'),
    ('NL', 'EU'),
    ('BE', 'EU'),
    ('AT', 'EU'),
    ('PT', 'EU'),
    ('IE', 'EU'),
    ('US', 'US'),
    ('GB', 'UK');