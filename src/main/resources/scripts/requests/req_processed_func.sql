INSERT INTO processed_functions (original_function_id, operation_id, resulting_function_id, parameters)
VALUES (?, ?, ?, ?);
SELECT * FROM processed_functions WHERE id = ?;
SELECT * FROM processed_functions WHERE original_function_id = ?;
SELECT * FROM processed_functions WHERE operation_id = ?;
UPDATE processed_functions SET parameters = ? WHERE id = ?;
DELETE FROM processed_functions WHERE id = ?;