INSERT INTO processed_functions (function_id, operation_id, result_summary) VALUES (?, ?, ?);

SELECT * FROM processed_functions WHERE id = ?;
SELECT * FROM processed_functions WHERE function_id = ?;
SELECT * FROM processed_functions WHERE operation_id = ?;

-- Все обработанные функции пользователя с результатами
SELECT pf.*, o.name as operation_name, rv.point_index, rv.x, rv.y
FROM processed_functions pf JOIN functions f ON pf.function_id = f.id
JOIN operations o ON pf.operation_id = o.id
LEFT JOIN result_values rv ON pf.id = rv.processed_function_id WHERE f.user_id = ?;

UPDATE processed_functions SET result_summary = ? WHERE id = ?;

DELETE FROM processed_functions WHERE id = ?;