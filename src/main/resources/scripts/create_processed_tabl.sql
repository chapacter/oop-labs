CREATE TABLE processed_functions (
    id BIGSERIAL PRIMARY KEY,
    original_function_id BIGINT NOT NULL,
    operation_id BIGINT NOT NULL,
    resulting_function_id BIGINT NOT NULL,
    parameters TEXT,
    processed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (original_function_id) REFERENCES functions(id) ON DELETE CASCADE,
    FOREIGN KEY (operation_id) REFERENCES operations(id) ON DELETE CASCADE,
    FOREIGN KEY (resulting_function_id) REFERENCES functions(id) ON DELETE CASCADE
);