INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?);

SELECT * FROM users WHERE id = ?;
SELECT * FROM users WHERE username = ?;
SELECT * FROM users WHERE email = ?;

-- Все пользователи
SELECT * FROM users ORDER BY name;

SELECT * FROM functions WHERE name LIKE ?;
SELECT * FROM functions WHERE username LIKE ?;
SELECT * FROM functions WHERE email LIKE ?;

-- Пользователь с логином и паролем
SELECT * FROM users WHERE login = ? AND password_hash = ?;

-- Статистика
SELECT
    f.id,
    f.name,
    COUNT(p.id) as point_count,
    MIN(p.x_value) as min_x,
    MAX(p.x_value) as max_x,
    AVG(p.y_value) as avg_y
FROM functions f LEFT JOIN points p ON f.id = p.function_id WHERE f.user_id = ? GROUP BY f.id, f.name;

-- Всё
UPDATE users SET username = ?, email = ? WHERE id = ?, password_hash = ? WHERE id = ?;

-- Отдельные
UPDATE users SET username = ? WHERE id = ?;
UPDATE users SET email = ? WHERE id = ?;
UPDATE users SET password_hash = ? WHERE id = ?;

DELETE FROM users WHERE id = ?;