INSERT INTO points (function_id, point_index, x, y) VALUES (?, ?, ?, ?);

-- Поиск (или чтение?)
SELECT * FROM points WHERE id = ?;
SELECT * FROM points WHERE function_id = ? ORDER BY point_index;
SELECT * FROM points WHERE x = ?;
SELECT * FROM points WHERE y = ?;
SELECT * FROM points WHERE x BETWEEN ? AND ?;
SELECT * FROM points WHERE y BETWEEN ? AND ?;
SELECT * FROM points WHERE function_id IN (?, ?, ?) ORDER BY function_id, point_index;
SELECT * FROM points WHERE function_id = ? ORDER BY x ASC;
SELECT * FROM points WHERE function_id = ? ORDER BY y ASC;
SELECT * FROM points WHERE function_id = ? ORDER BY x DESC;
SELECT * FROM points WHERE function_id = ? ORDER BY y DESC;

-- Все т-ки пользователя
SELECT p.* FROM points p JOIN functions f ON p.function_id = f.id WHERE f.user_id = ?;

UPDATE points SET point_index = ?, x = ?, y = ? WHERE id = ?;

сDELETE FROM points WHERE id = ?;
DELETE FROM points WHERE function_id = ?;