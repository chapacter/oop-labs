CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    access_lvl INTEGER,
    password_hash VARCHAR(255)
);

CREATE TABLE functions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    format INTEGER,
    func_result VARCHAR(2048),
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    type VARCHAR(50) DEFAULT 'TABULATED',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE operations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1024)
);

CREATE TABLE points (
    id BIGSERIAL PRIMARY KEY,
    function_id BIGINT NOT NULL,
    point_index INTEGER NOT NULL,
    x DOUBLE PRECISION NOT NULL,
    y DOUBLE PRECISION NOT NULL,
    FOREIGN KEY (function_id) REFERENCES functions(id) ON DELETE CASCADE
);

CREATE TABLE processed_functions (
    id BIGSERIAL PRIMARY KEY,
    function_id BIGINT NOT NULL,
    operation_id BIGINT NOT NULL,
    result_summary VARCHAR(2048),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (function_id) REFERENCES functions(id) ON DELETE CASCADE,
    FOREIGN KEY (operation_id) REFERENCES operations(id) ON DELETE CASCADE
);

CREATE TABLE result_values (
    id BIGSERIAL PRIMARY KEY,
    processed_function_id BIGINT NOT NULL,
    point_index INTEGER,
    x DOUBLE PRECISION,
    y DOUBLE PRECISION,
    FOREIGN KEY (processed_function_id) REFERENCES processed_functions(id) ON DELETE CASCADE
);

CREATE TABLE tabulated_func (
  id SERIAL PRIMARY KEY,
  func_id INT NOT NULL REFERENCES functions(id) ON DELETE CASCADE,
  x_val DOUBLE PRECISION NOT NULL,
  y_val DOUBLE PRECISION NOT NULL
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='functions' AND column_name='point_count') THEN
        ALTER TABLE functions ADD COLUMN point_count INTEGER DEFAULT 0;
    END IF;
END $$;