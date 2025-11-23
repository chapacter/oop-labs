CREATE TABLE result_values (
    id BIGSERIAL PRIMARY KEY,
    processed_function_id BIGINT NOT NULL,
    point_index INTEGER,
    x DOUBLE PRECISION,
    y DOUBLE PRECISION,
    FOREIGN KEY (processed_function_id) REFERENCES processed_functions(id) ON DELETE CASCADE
);