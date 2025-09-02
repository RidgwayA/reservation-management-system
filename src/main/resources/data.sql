-- Insert 75 RV sites (1-75) distributed across locations
-- Woods: Sites 1-20 (20 RV sites)
INSERT INTO campsites (site_number, site_type, status, location, active, created_at, updated_at) 
SELECT 
    generate_series(1, 20),
    'FULL_HOOKUP',
    'AVAILABLE',
    'WOODS',
    true, NOW(), NOW()
ON CONFLICT (site_number) DO NOTHING;

-- ATV Park: Sites 21-35 (15 RV sites)
INSERT INTO campsites (site_number, site_type, status, location, active, created_at, updated_at) 
SELECT 
    generate_series(21, 35),
    'FULL_HOOKUP',
    'AVAILABLE',
    'ATV_PARK',
    true, NOW(), NOW()
ON CONFLICT (site_number) DO NOTHING;

-- Lake: Sites 36-55 (20 RV sites)
INSERT INTO campsites (site_number, site_type, status, location, active, created_at, updated_at) 
SELECT 
    generate_series(36, 55),
    'FULL_HOOKUP',
    'AVAILABLE',
    'LAKE',
    true, NOW(), NOW()
ON CONFLICT (site_number) DO NOTHING;

-- Base Camp: Sites 56-75 (20 RV sites)
INSERT INTO campsites (site_number, site_type, status, location, active, created_at, updated_at) 
SELECT 
    generate_series(56, 75),
    'FULL_HOOKUP',
    'AVAILABLE',
    'BASE_CAMP',
    true, NOW(), NOW()
ON CONFLICT (site_number) DO NOTHING;

-- Insert 125 tent sites (76-200) - all in BASE_CAMP location
INSERT INTO campsites (site_number, site_type, status, location, active, created_at, updated_at)
SELECT 
    generate_series(76, 200),
    'TENT',
    'AVAILABLE', 
    'BASE_CAMP',
    true, NOW(), NOW()
ON CONFLICT (site_number) DO NOTHING;

-- Insert test customers
INSERT INTO customers (first_name, last_name, email, phone, active, created_at, updated_at) VALUES
('John', 'Smith', 'john.smith@email.com', '555-0101', true, NOW(), NOW()),
('Sarah', 'Johnson', 'sarah.j@email.com', '555-0102', true, NOW(), NOW()),
('Mike', 'Wilson', 'mike.wilson@email.com', '555-0103', true, NOW(), NOW()),
('Emily', 'Brown', 'emily.brown@email.com', '555-0104', true, NOW(), NOW()),
('David', 'Davis', 'david.davis@email.com', '555-0105', true, NOW(), NOW());

-- Insert dynamic test reservations (always relative to current date)
-- Today's check-ins (CONFIRMED status)
INSERT INTO reservations (
    customer_id, campsite_id, start_date, end_date, status, party_size,
    license_plate, vehicle_make, vehicle_model, rv_length_feet,
    campsite_total, currency, atv_total, atv_currency, total_amount, total_currency, 
    paid_amount, paid_currency, confirmation_number, notes, active, created_at, updated_at
)
SELECT 
    c.id, cs.id,
    CURRENT_DATE, CURRENT_DATE + INTERVAL '3 days',
    'CONFIRMED', 4,
    'ABC123', 'Ford', 'F-150', 32,
    150.00, 'USD', 0.00, 'USD', 450.00, 'USD',
    450.00, 'USD', 'CONF-1001',
    'Check-in today - RV site', true, NOW(), NOW()
FROM customers c, campsites cs 
WHERE c.email = 'john.smith@email.com' AND cs.site_number = 15
ON CONFLICT DO NOTHING;

-- Today's check-outs (CHECKED_IN status)  
INSERT INTO reservations (
    customer_id, campsite_id, start_date, end_date, status, party_size,
    license_plate, vehicle_make, vehicle_model, rv_length_feet,
    campsite_total, currency, atv_total, atv_currency, total_amount, total_currency,
    paid_amount, paid_currency, confirmation_number, notes, check_in_time,
    active, created_at, updated_at
)
SELECT 
    c.id, cs.id,
    CURRENT_DATE - INTERVAL '2 days', CURRENT_DATE,
    'CHECKED_IN', 2,
    'XYZ789', 'Winnebago', 'Vista', 28,
    120.00, 'USD', 0.00, 'USD', 360.00, 'USD',
    360.00, 'USD', 'CONF-1002',
    'Check-out today - Lake view RV', NOW() - INTERVAL '2 days',
    true, NOW(), NOW()
FROM customers c, campsites cs 
WHERE c.email = 'sarah.j@email.com' AND cs.site_number = 45
ON CONFLICT (confirmation_number) DO NOTHING;

-- Currently occupied sites (CHECKED_IN, staying multiple days)
INSERT INTO reservations (
    customer_id, campsite_id, start_date, end_date, status, party_size,
    license_plate, vehicle_make, vehicle_model, rv_length_feet,
    campsite_total, currency, atv_total, atv_currency, total_amount, total_currency,
    paid_amount, paid_currency, confirmation_number, notes, check_in_time,
    active, created_at, updated_at
)
SELECT 
    c.id, cs.id,
    CURRENT_DATE - INTERVAL '1 day', CURRENT_DATE + INTERVAL '2 days',
    'CHECKED_IN', 6,
    'RV2024', 'Thor', 'Ace', 35,
    180.00, 'USD', 60.00, 'USD', 720.00, 'USD',
    720.00, 'USD', 'CONF-1003',
    'Multi-day stay with ATV passes', NOW() - INTERVAL '1 day',
    true, NOW(), NOW()
FROM customers c, campsites cs 
WHERE c.email = 'mike.wilson@email.com' AND cs.site_number = 25
ON CONFLICT (confirmation_number) DO NOTHING;

-- Future reservations (CONFIRMED)
INSERT INTO reservations (
    customer_id, campsite_id, start_date, end_date, status, party_size,
    license_plate, vehicle_make, vehicle_model,
    campsite_total, currency, atv_total, atv_currency, total_amount, total_currency, 
    paid_amount, paid_currency, confirmation_number, notes, active, created_at, updated_at
)
SELECT 
    c.id, cs.id,
    CURRENT_DATE + INTERVAL '2 days', CURRENT_DATE + INTERVAL '4 days',
    'CONFIRMED', 3,
    'TENT01', NULL, NULL,
    90.00, 'USD', 0.00, 'USD', 270.00, 'USD',
    135.00, 'USD', 'CONF-1004',
    'Tent camping - partial payment', true, NOW(), NOW()
FROM customers c, campsites cs 
WHERE c.email = 'emily.brown@email.com' AND cs.site_number = 185
ON CONFLICT (confirmation_number) DO NOTHING;

-- Weekend reservation starting tomorrow (CONFIRMED)
INSERT INTO reservations (
    customer_id, campsite_id, start_date, end_date, status, party_size,
    license_plate, vehicle_make, vehicle_model, rv_length_feet,
    campsite_total, currency, atv_total, atv_currency, total_amount, total_currency,
    paid_amount, paid_currency, confirmation_number, notes, active, created_at, updated_at
)
SELECT 
    c.id, cs.id,
    CURRENT_DATE + INTERVAL '1 day', CURRENT_DATE + INTERVAL '3 days',
    'CONFIRMED', 4,
    'WKND99', 'Jayco', 'Eagle', 30,
    160.00, 'USD', 40.00, 'USD', 640.00, 'USD',
    320.00, 'USD', 'CONF-1005',
    'Weekend getaway - 50% deposit paid', true, NOW(), NOW()
FROM customers c, campsites cs 
WHERE c.email = 'david.davis@email.com' AND cs.site_number = 55
ON CONFLICT (confirmation_number) DO NOTHING;