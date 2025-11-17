INSERT INTO operations (name, description) VALUES (?, ?);

SELECT * FROM operations WHERE id = ?;
SELECT * FROM operations WHERE name = ?;

UPDATE operations SET description = ? WHERE id = ?;

DELETE FROM operations WHERE id = ?;