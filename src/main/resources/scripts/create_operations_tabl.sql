DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'operations') THEN
        CREATE TABLE operations (
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(255) NOT NULL UNIQUE,
            description VARCHAR(1024)
        );
    END IF;
END $$;