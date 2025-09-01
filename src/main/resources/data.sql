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
INSERT INTO customers (first_name, last_name, email, phone, address_line1, city, state, zip_code, created_at, updated_at) VALUES
('John', 'Smith', 'john.smith@email.com', '555-0101', '123 Main St', 'Springfield', 'IL', '62701', NOW(), NOW()),
('Sarah', 'Johnson', 'sarah.j@email.com', '555-0102', '456 Oak Ave', 'Madison', 'WI', '53703', NOW(), NOW()),
('Mike', 'Wilson', 'mike.wilson@email.com', '555-0103', '789 Pine Rd', 'Austin', 'TX', '78701', NOW(), NOW()),
('Emily', 'Brown', 'emily.brown@email.com', '555-0104', '321 Elm St', 'Denver', 'CO', '80202', NOW(), NOW()),
('David', 'Davis', 'david.davis@email.com', '555-0105', '654 Cedar Ln', 'Portland', 'OR', '97201', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Insert dynamic test reservations (always relative to current date)
-- Today's check-ins (CONFIRMED status)
INSERT INTO reservations (
    customer_id, campsite_id, start_date, end_date, status, party_size,
    license_plate, vehicle_make, vehicle_model, rv_length_feet,
    campsite_total, atv_total, total_amount, paid_amount,
    confirmation_number, notes, created_at, updated_at
)
SELECT 
    c.id,
    cs.id,
    CURRENT_DATE,
    CURRENT_DATE + INTERVAL '3 days',
    'CONFIRMED',
    4,
    'ABC123',
    'Ford',
    'F-150',
    32,
    150.00, 0.00, 450.00, 450.00,
    'CONF-' || LPAD((RANDOM() * 9999)::int::text, 4, '0'),
    'Check-in today - RV site',
    NOW(), NOW()
FROM customers c, campsites cs 
WHERE c.email = 'john.smith@email.com' AND cs.site_number = 15
ON CONFLICT DO NOTHING;

-- Today's check-outs (CHECKED_IN status)  
INSERT INTO reservations (
    customer_id, campsite_id, start_date, end_date, status, party_size,
    license_plate, vehicle_make, vehicle_model, rv_length_feet,
    campsite_total, atv_total, total_amount, paid_amount,
    confirmation_number, notes, check_in_time, created_at, updated_at
)
SELECT 
    c.id,
    cs.id,
    CURRENT_DATE - INTERVAL '2 days',
    CURRENT_DATE,
    'CHECKED_IN',
    2,
    'XYZ789',
    'Winnebago',
    'Vista',
    28,
    120.00, 0.00, 360.00, 360.00,
    'CONF-' || LPAD((RANDOM() * 9999)::int::text, 4, '0'),
    'Check-out today - Lake view RV',
    NOW() - INTERVAL '2 days',
    NOW(), NOW()
FROM customers c, campsites cs 
WHERE c.email = 'sarah.j@email.com' AND cs.site_number = 45
ON CONFLICT DO NOTHING;

-- Currently occupied sites (CHECKED_IN, staying multiple days)
INSERT INTO reservations (
    customer_id, campsite_id, start_date, end_date, status, party_size,
    license_plate, vehicle_make, vehicle_model, rv_length_feet,
    campsite_total, atv_total, total_amount, paid_amount,
    confirmation_number, notes, check_in_time, created_at, updated_at
)
SELECT 
    c.id,
    cs.id,
    CURRENT_DATE - INTERVAL '1 day',
    CURRENT_DATE + INTERVAL '2 days',
    'CHECKED_IN',
    6,
    'RV2024',
    'Thor',
    'Ace',
    35,
    180.00, 60.00, 720.00, 720.00,
    'CONF-' || LPAD((RANDOM() * 9999)::int::text, 4, '0'),
    'Multi-day stay with ATV passes',
    NOW() - INTERVAL '1 day',
    NOW(), NOW()
FROM customers c, campsites cs 
WHERE c.email = 'mike.wilson@email.com' AND cs.site_number = 25
ON CONFLICT DO NOTHING;

-- Future reservations (CONFIRMED)
INSERT INTO reservations (
    customer_id, campsite_id, start_date, end_date, status, party_size,
    license_plate, vehicle_make, vehicle_model,
    campsite_total, atv_total, total_amount, paid_amount,
    confirmation_number, notes, created_at, updated_at
)
SELECT 
    c.id,
    cs.id,
    CURRENT_DATE + INTERVAL '2 days',
    CURRENT_DATE + INTERVAL '4 days',
    'CONFIRMED',
    3,
    'TENT01',
    NULL,
    NULL,
    90.00, 0.00, 270.00, 135.00,
    'CONF-' || LPAD((RANDOM() * 9999)::int::text, 4, '0'),
    'Tent camping - partial payment',
    NOW(), NOW()
FROM customers c, campsites cs 
WHERE c.email = 'emily.brown@email.com' AND cs.site_number = 185
ON CONFLICT DO NOTHING;

-- Weekend reservation starting tomorrow (CONFIRMED)
INSERT INTO reservations (
    customer_id, campsite_id, start_date, end_date, status, party_size,
    license_plate, vehicle_make, vehicle_model, rv_length_feet,
    campsite_total, atv_total, total_amount, paid_amount,
    confirmation_number, notes, created_at, updated_at
)
SELECT 
    c.id,
    cs.id,
    CURRENT_DATE + INTERVAL '1 day',
    CURRENT_DATE + INTERVAL '3 days',
    'CONFIRMED',
    4,
    'WKND99',
    'Jayco',
    'Eagle',
    30,
    160.00, 40.00, 640.00, 320.00,
    'CONF-' || LPAD((RANDOM() * 9999)::int::text, 4, '0'),
    'Weekend getaway - 50% deposit paid',
    NOW(), NOW()
FROM customers c, campsites cs 
WHERE c.email = 'david.davis@email.com' AND cs.site_number = 55
ON CONFLICT DO NOTHING;