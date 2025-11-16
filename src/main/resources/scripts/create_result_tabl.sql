CREATE TABLE result_values (
    id BIGSERIAL PRIMARY KEY,
    processed_function_id BIGINT NOT NULL,
    key VARCHAR(255) NOT NULL,
    value DECIMAL,
    value_type VARCHAR(50),
    FOREIGN KEY (processed_function_id) REFERENCES processed_functions(id) ON DELETE CASCADE
);