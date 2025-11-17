INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?);
SELECT * FROM users WHERE id = ?;
SELECT * FROM users WHERE username = ?;
UPDATE users SET username = ?, email = ?, password_hash = ? WHERE id = ?;
DELETE FROM users WHERE id = ?;