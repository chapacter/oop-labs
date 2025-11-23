CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    access_lvl INTEGER,
    password_hash VARCHAR(255)
);