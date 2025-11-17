INSERT INTO points (function_id, x_value, y_value, point_order) VALUES (?, ?, ?, ?);

-- По конкретным
SELECT * FROM points WHERE id = ?;
SELECT * FROM points WHERE function_id = ? ORDER BY point_order;
SELECT * FROM points WHERE x_value = ?;
SELECT * FROM points WHERE y_value = ?;

-- По диапазону
SELECT * FROM points WHERE x_value BETWEEN ? AND ?;
SELECT * FROM points WHERE y_value BETWEEN ? AND ?;

-- По нескольким func_id
SELECT * FROM points WHERE function_id IN (?, ?, ?) ORDER BY function_id, point_order;

-- По id с сортировкой по опр точкамv
SELECT * FROM points WHERE function_id = ? ORDER BY x_value ASC;
SELECT * FROM points WHERE function_id = ? ORDER BY y_value ASC;

-- В обраьную
SELECT * FROM points WHERE function_id = ? ORDER BY x_value DESC;
SELECT * FROM points WHERE function_id = ? ORDER BY y_value DESC;

-- Все т-ки пользователя
SELECT p.*
FROM points p
JOIN functions f ON p.function_id = f.id
WHERE f.user_id = ?;

-- Все обработанные для пользователя
SELECT pf.*, rv.key, rv.value FROM processed_functions pf JOIN functions f ON pf.original_function_id = f.id
LEFT JOIN result_values rv ON pf.id = rv.processed_function_id WHERE f.user_id = ?;

UPDATE points SET x_value = ?, y_value = ?, point_order = ? WHERE id = ?;

DELETE FROM points WHERE id = ?;
DELETE FROM points WHERE function_id = ?;