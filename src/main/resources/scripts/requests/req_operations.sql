INSERT INTO operations (name, description) VALUES (?, ?);

-- Поиск
SELECT * FROM operations WHERE id = ?;
SELECT * FROM operations WHERE name = ?;
SELECT * FROM operations;

UPDATE operations SET name = ?, description = ? WHERE id = ?;
UPDATE operations SET description = ? WHERE id = ?;

DELETE FROM operations WHERE id = ?;