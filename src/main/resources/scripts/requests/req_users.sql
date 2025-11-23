INSERT INTO users (name, access_lvl, password_hash) VALUES (?, ?, ?);

-- Поиск
SELECT * FROM users WHERE id = ?;
SELECT * FROM users WHERE name = ?;
SELECT * FROM users ORDER BY name;

-- Для авторизации
SELECT * FROM users WHERE name = ? AND password_hash = ?;

UPDATE users SET name = ?, access_lvl = ?, password_hash = ? WHERE id = ?;
UPDATE users SET name = ? WHERE id = ?;
UPDATE users SET access_lvl = ? WHERE id = ?;
UPDATE users SET password_hash = ? WHERE id = ?;

DELETE FROM users WHERE id = ?;

-- Статистика для пользователя
SELECT
    f.id,
    f.name,
    COUNT(p.id) as point_count,
    MIN(p.x) as min_x,
    MAX(p.x) as max_x,
    AVG(p.y) as avg_y
FROM functions f LEFT JOIN points p ON f.id = p.function_id WHERE f.user_id = ? GROUP BY f.id, f.name;