-- Создание таблицы ролей
CREATE TABLE roles
(
    id        UUID PRIMARY KEY,
    role_name VARCHAR(255) NOT NULL UNIQUE
);

-- Создание таблицы пользователей
CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    username   VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL
);

-- Создание таблицы валют
CREATE TABLE currencies
(
    id            UUID PRIMARY KEY,
    code          VARCHAR(10)    NOT NULL UNIQUE,
    name          VARCHAR(255)   NOT NULL,
    exchange_rate DECIMAL(10, 6) NOT NULL,
    last_updated  TIMESTAMP      NOT NULL
);

-- Создание таблицы настроек пользователя
CREATE TABLE settings
(
    id                    UUID PRIMARY KEY,
    user_id               UUID NOT NULL,
    preferred_currency_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (preferred_currency_id) REFERENCES currencies (id) ON DELETE CASCADE
);

-- Создание таблицы ролей пользователей (связующая таблица)
CREATE TABLE user_roles
(
    id      UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);
