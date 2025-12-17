DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tabulated_func') THEN
        CREATE TABLE tabulated_func (
            id SERIAL PRIMARY KEY,
            func_id INT NOT NULL REFERENCES functions(id) ON DELETE CASCADE,
            x_val DOUBLE PRECISION NOT NULL,
            y_val DOUBLE PRECISION NOT NULL
        );
    END IF;
END $$;
