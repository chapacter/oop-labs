DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') THEN
        CREATE TABLE users (
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(255) NOT NULL UNIQUE,
            access_lvl INTEGER,
            password_hash VARCHAR(255)
        );
    END IF;
END $$;