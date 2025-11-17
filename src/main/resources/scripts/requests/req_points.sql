INSERT INTO points (function_id, x_value, y_value, point_order) VALUES (?, ?, ?, ?);

-- По конкретным
SELECT * FROM points WHERE id = ?;
SELECT * FROM points WHERE function_id = ? ORDER BY point_order;
SELECT * FROM points WHERE x_value = ?;
SELECT * FROM points WHERE y_value = ?;
SELECT * FROM points WHERE point_order = ?;

-- По диапазону
SELECT * FROM points WHERE x_value BETWEEN ? AND ?;
SELECT * FROM points WHERE y_value BETWEEN ? AND ?;

-- По нескольким func_id
SELECT * FROM points WHERE function_id IN (?, ?, ?) ORDER BY function_id, point_order;

UPDATE points SET x_value = ?, y_value = ?, point_order = ? WHERE id = ?;

DELETE FROM points WHERE id = ?;
DELETE FROM points WHERE function_id = ?;