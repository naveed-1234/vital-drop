INSERT INTO admins (username, password)
SELECT 'naveed', '$2a$10$Xix1sgSsU6wEAdxz4/qm7OEgbM56T3KYMWdN4BGjb.OICDxzb88Q2'
WHERE NOT EXISTS (SELECT 1 FROM admins WHERE username = 'naveed');