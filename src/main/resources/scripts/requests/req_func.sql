INSERT INTO functions (name, description, user_id) VALUES (?, ?, ?);
SELECT * FROM functions WHERE id = ?;
SELECT * FROM functions WHERE user_id = ?;
UPDATE functions SET name = ?, description = ? WHERE id = ?;
DELETE FROM functions WHERE id = ?;