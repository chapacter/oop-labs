INSERT INTO functions (name, format, func_result, user_id) VALUES (?, ?, ?, ?);

-- Поиск
SELECT * FROM functions WHERE id = ?;
SELECT * FROM functions WHERE user_id = ?;
SELECT * FROM functions WHERE name = ?;
SELECT * FROM functions WHERE name LIKE ?;
SELECT * FROM functions WHERE user_id = ? AND name LIKE ? AND created_at BETWEEN ? AND ?;
SELECT * FROM functions WHERE user_id = ? ORDER BY name ASC;

-- Обновление
UPDATE functions SET name = ?, format = ?, func_result = ? WHERE id = ?;
UPDATE functions SET name = ? WHERE id = ?;
UPDATE functions SET format = ? WHERE id = ?;
UPDATE functions SET func_result = ? WHERE id = ?;

DELETE FROM functions WHERE id = ?;