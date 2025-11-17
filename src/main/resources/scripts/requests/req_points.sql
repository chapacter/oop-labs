INSERT INTO points (function_id, x_value, y_value, point_order) VALUES (?, ?, ?, ?);
SELECT * FROM points WHERE id = ?;
SELECT * FROM points WHERE function_id = ? ORDER BY point_order;
UPDATE points SET x_value = ?, y_value = ?, point_order = ? WHERE id = ?;
DELETE FROM points WHERE id = ?;
DELETE FROM points WHERE function_id = ?;