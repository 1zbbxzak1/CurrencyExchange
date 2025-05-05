-- Удаляем записи из таблицы user_roles, которые ссылаются на роли
DELETE
FROM user_roles
WHERE role_id IN (SELECT id FROM roles);

-- Удаляем записи из таблиц
DELETE
FROM user_roles;
DELETE
FROM settings;
DELETE
FROM users;
DELETE
FROM currencies;
DELETE
FROM roles;

-- Вставка ролей
INSERT INTO roles (id, role_name)
VALUES ('e4eaaaf2-d142-11e9-b151-0800200c9a66', 'role_admin'),
       ('e4eaaaf2-d142-11e9-b151-0800200c9a67', 'role_user');

-- Вставка валют
INSERT INTO currencies (id, code, name, exchange_rate, last_updated)
VALUES ('d84b7d8a-d142-11e9-b151-0800200c9a68', 'USD', 'US Dollar', 1.0, CURRENT_TIMESTAMP),
       ('d84b7d8a-d142-11e9-b151-0800200c9a69', 'EUR', 'Euro', 0.85, CURRENT_TIMESTAMP);

-- Вставка пользователей
INSERT INTO users (id, username, password, created_at)
VALUES ('5fa1fa40-d142-11e9-b151-0800200c9a70', 'testuser',
        '$2a$10$kw7sNWvoh28c4jac0ExlkuoIT.NGyv2zuGKqRVLyEm8s4CK3d4C5K', CURRENT_TIMESTAMP),
       ('5fa1fa40-d142-11e9-b151-0800200c9a71', 'deleteuser',
        '$2a$10$kw7sNWvoh28c4jac0ExlkuoIT.NGyv2zuGKqRVLyEm8s4CK3d4C5K', CURRENT_TIMESTAMP);

-- Вставка настроек пользователя
INSERT INTO settings (id, user_id, preferred_currency_id)
VALUES ('d84b7d8a-d142-11e9-b151-0800200c9a72', '5fa1fa40-d142-11e9-b151-0800200c9a70',
        'd84b7d8a-d142-11e9-b151-0800200c9a68'),
       ('d84b7d8a-d142-11e9-b151-0800200c9a73', '5fa1fa40-d142-11e9-b151-0800200c9a71',
        'd84b7d8a-d142-11e9-b151-0800200c9a69');

-- Вставка ролей для пользователей
INSERT INTO user_roles (id, user_id, role_id)
VALUES ('d84b7d8a-d142-11e9-b151-0800200c9a74', '5fa1fa40-d142-11e9-b151-0800200c9a70',
        'e4eaaaf2-d142-11e9-b151-0800200c9a66'),
       ('d84b7d8a-d142-11e9-b151-0800200c9a75', '5fa1fa40-d142-11e9-b151-0800200c9a71',
        'e4eaaaf2-d142-11e9-b151-0800200c9a67');
