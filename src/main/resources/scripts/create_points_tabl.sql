CREATE TABLE points (
    id BIGSERIAL PRIMARY KEY,
    function_id BIGINT NOT NULL,
    x_value DECIMAL NOT NULL,
    y_value DECIMAL NOT NULL,
    point_order INT NOT NULL,
    FOREIGN KEY (function_id) REFERENCES functions(id) ON DELETE CASCADE
);