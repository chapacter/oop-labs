INSERT INTO result_values (processed_function_id, point_index, x, y) VALUES (?, ?, ?, ?);

-- Поиск
SELECT * FROM result_values WHERE id = ?;
SELECT * FROM result_values WHERE processed_function_id = ?;
SELECT * FROM result_values WHERE processed_function_id = ? ORDER BY point_index;

UPDATE result_values SET point_index = ?, x = ?, y = ? WHERE id = ?;

DELETE FROM result_values WHERE id = ?;
DELETE FROM result_values WHERE processed_function_id = ?;