INSERT INTO result_values (processed_function_id, key, value, value_type) VALUES (?, ?, ?, ?);

SELECT * FROM result_values WHERE id = ?;
SELECT * FROM result_values WHERE processed_function_id = ?;
SELECT * FROM result_values WHERE value_type = ?;

UPDATE result_values SET value = ?, value_type = ? WHERE id = ?;

DELETE FROM result_values WHERE id = ?;