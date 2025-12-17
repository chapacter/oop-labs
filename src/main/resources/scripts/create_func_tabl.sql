DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'functions') THEN
        CREATE TABLE functions (
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            format INTEGER,
            func_result VARCHAR(2048),
            user_id BIGINT NOT NULL,
            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        );
    END IF;
END $$;