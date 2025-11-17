INSERT INTO functions (name, description, user_id) VALUES (?, ?, ?);

-- Конкретный
SELECT * FROM functions WHERE id = ?;
SELECT * FROM functions WHERE user_id = ?;
SELECT * FROM functions WHERE name = ?;

-- Частичное совпадение
SELECT * FROM functions WHERE name LIKE ?;

-- Множественный
SELECT * FROM functions WHERE user_id = ? AND name LIKE ? AND created_at BETWEEN ? AND ?;

-- С сортировкой по имени
SELECT * FROM functions WHERE user_id = ? ORDER BY name ASC;

UPDATE functions SET name = ? WHERE id = ?;
UPDATE functions SET description = ? WHERE id = ?;

DELETE FROM functions WHERE id = ?;