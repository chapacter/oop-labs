INSERT INTO operations (name, description) VALUES (?, ?);

--Конкретынй
SELECT * FROM operations WHERE id = ?;
SELECT * FROM operations WHERE name = ?;

-- По нескольким
SELECT * FROM functions
WHERE user_id = ?
AND name LIKE ?
AND created_at BETWEEN ? AND ?;

UPDATE operations SET description = ? WHERE id = ?;

DELETE FROM operations WHERE id = ?;