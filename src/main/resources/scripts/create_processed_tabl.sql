DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'processed_functions') THEN
        CREATE TABLE processed_functions (
            id BIGSERIAL PRIMARY KEY,
            function_id BIGINT NOT NULL,
            operation_id BIGINT NOT NULL,
            result_summary VARCHAR(2048),
            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (function_id) REFERENCES functions(id) ON DELETE CASCADE,
            FOREIGN KEY (operation_id) REFERENCES operations(id) ON DELETE CASCADE
        );
    END IF;
END $$;