-- liquibase formatted sql

-- changeset accou:1750685705795-1
CREATE TABLE currencies
(
    id            VARCHAR(255)                NOT NULL,
    code          VARCHAR(10)                 NOT NULL,
    name          VARCHAR(255)                NOT NULL,
    exchange_rate DECIMAL(10, 6)              NOT NULL,
    last_updated  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_currencies PRIMARY KEY (id)
);

-- changeset accou:1750685705795-2
CREATE TABLE currency_conversions
(
    id                 VARCHAR(255)                NOT NULL,
    user_id            VARCHAR(255)                NOT NULL,
    source_currency_id VARCHAR(255)                NOT NULL,
    target_currency_id VARCHAR(255)                NOT NULL,
    amount             DECIMAL(18, 6)              NOT NULL,
    converted_amount   DECIMAL(18, 6)              NOT NULL,
    conversion_rate    DECIMAL(18, 6)              NOT NULL,
    timestamp          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_currency_conversions PRIMARY KEY (id)
);

-- changeset accou:1750685705795-3
CREATE TABLE report
(
    id         VARCHAR(255)                NOT NULL,
    status     VARCHAR(255)                NOT NULL,
    file_path  VARCHAR(255)                NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_report PRIMARY KEY (id)
);

-- changeset accou:1750685705795-4
CREATE TABLE roles
(
    id        VARCHAR(255) NOT NULL,
    role_name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

-- changeset accou:1750685705795-5
CREATE TABLE settings
(
    id                    VARCHAR(255) NOT NULL,
    user_id               VARCHAR(255) NOT NULL,
    preferred_currency_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_settings PRIMARY KEY (id)
);

-- changeset accou:1750685705795-6
CREATE TABLE user_roles
(
    id      VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    role_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (id)
);

-- changeset accou:1750685705795-7
CREATE TABLE users
(
    id         VARCHAR(255)                NOT NULL,
    chat_id    BIGINT                      NOT NULL,
    username   VARCHAR(255)                NOT NULL,
    email      VARCHAR(255)                NOT NULL,
    password   VARCHAR(255)                NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

-- changeset accou:1750685705795-8
ALTER TABLE currencies
    ADD CONSTRAINT uc_currencies_code UNIQUE (code);

-- changeset accou:1750685705795-9
ALTER TABLE roles
    ADD CONSTRAINT uc_roles_role_name UNIQUE (role_name);

-- changeset accou:1750685705795-10
ALTER TABLE settings
    ADD CONSTRAINT uc_settings_user UNIQUE (user_id);

-- changeset accou:1750685705795-11
ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

-- changeset accou:1750685705795-12
ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

-- changeset accou:1750685705795-13
ALTER TABLE currency_conversions
    ADD CONSTRAINT FK_CURRENCY_CONVERSIONS_ON_SOURCE_CURRENCY FOREIGN KEY (source_currency_id) REFERENCES currencies (id);

-- changeset accou:1750685705795-14
ALTER TABLE currency_conversions
    ADD CONSTRAINT FK_CURRENCY_CONVERSIONS_ON_TARGET_CURRENCY FOREIGN KEY (target_currency_id) REFERENCES currencies (id);

-- changeset accou:1750685705795-15
ALTER TABLE currency_conversions
    ADD CONSTRAINT FK_CURRENCY_CONVERSIONS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

-- changeset accou:1750685705795-16
ALTER TABLE settings
    ADD CONSTRAINT FK_SETTINGS_ON_PREFERRED_CURRENCY FOREIGN KEY (preferred_currency_id) REFERENCES currencies (id);

-- changeset accou:1750685705795-17
ALTER TABLE settings
    ADD CONSTRAINT FK_SETTINGS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

-- changeset accou:1750685705795-18
ALTER TABLE user_roles
    ADD CONSTRAINT FK_USER_ROLES_ON_ROLE FOREIGN KEY (role_id) REFERENCES roles (id);

-- changeset accou:1750685705795-19
ALTER TABLE user_roles
    ADD CONSTRAINT FK_USER_ROLES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

