-- Insert 75 RV sites (1-75) - all available
INSERT INTO campsites (site_number, site_type, status, active, created_at, updated_at) 
SELECT 
    generate_series(1, 75),
    'FULL_HOOKUP',
    'AVAILABLE',
    true,
    NOW(),
    NOW()
ON CONFLICT (site_number) DO NOTHING;

-- Insert 125 tent sites (76-200) - all available  
INSERT INTO campsites (site_number, site_type, status, active, created_at, updated_at)
SELECT 
    generate_series(76, 200),
    'TENT',
    'AVAILABLE', 
    true,
    NOW(),
    NOW()
ON CONFLICT (site_number) DO NOTHING;