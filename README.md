# 💸 Конвертер валют

Сервис предоставляет возможность быстрого перевода валюта из одной в другую с сохранением истории конвертаций.

## 🌌 Стек технологий

- **Язык**: Java 21
- **Сборщик проекта**: Maven
- **Фреймворк**: Spring Boot 3.4.3
- **База данных**: PostgreSQL 17
- **Миграции БД**: Liquibase
- **Рассылка**: Spring Mail + Gmail SMTP

## 🎴 Установка проекта

```bash
git clone https://github.com/1zbbxzak1/CurrencyExchange.git
```

## ⚙️ Подготовка к запуску проекта

Перед запуском приложения создайте файл `.env` в корневой директории. В качестве примера используйте `.env.example`.

1. **Скопируйте** файл `.env.example`:
   ```sh
   cp .env.example .env
   ```
2. Заполните `.env` своими данными

***Также необходимо проверить и запустить заранее установленное ПО***:
- Docker Desktop
- PostgreSQL

### 📑 Содержимое .env файла

#### 🤖 Настройки telegram-бота

Для работы с telegram-ботом неодходимо указать его accessToken:
```properties
TELEGRAM_TOKEN=long-token
```

#### 📬 Настройка Gmail SMTP

Для настройки email-рассылки с кодами подтверждения необходимо указать данные в `.env`:

```properties
MAIL_HOST=mail_host
MAIL_PORT=mail_port
MAIL_USERNAME=email@gmail.com
MAIL_PASSWORD=email_password
MAIL_CONNECTION_TIMEOUT=number
MAIL_TIMEOUT=number
MAIL_WRITE_TIMEOUT=number
```

#### 🗃 Настройки базы данных PostgreSQL

Для настройки PostgreSQL необходимо указать параметры подключения к БД:

```properties
POSTGRES_USER=your_user
POSTGRES_PASSWORD=your_password
POSTGRES_DB=your_db
```

Далее необходимо настроить под себя переменные для подключения к БД при запуске docker-compose:
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/${POSTGRES_DB}
SPRING_DATASOURCE_USERNAME=${POSTGRES_USER}
SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD}
SPRING_PROFILES_ACTIVE=prod
```

Убедитесь, что:
- Создана база данных с именем, указанным в `POSTGRES_DB`
- Пользователь имеет необходимые права доступа

#### 📋 Настройка начальных пользователей

И наконец, небходимо указать данные для админа и обычного пользователя:

1. Указываем chatId, username, email и пароль админа:
```properties
ADMIN_CHAT_ID=chatId
ADMIN_USERNAME=admin
ADMIN_EMAIL=admin@mail.ru
ADMIN_PASSWORD=password
```
2. Указываем chatId, username, email и пароль юзера:
```properties
USER_CHAT_ID=chatId
USER_USERNAME=user
USER_EMAIL=user@mail.ru
USER_PASSWORD=password
```

Эти учетные данные будут использованы при первом запуске приложения для создания администратора и обычного пользователя сервиса.

## 🚀 Запуск проекта

### 📦 Сборка

```bash
mvn clean install
```

## 🌀 Запуск приложения

```bash
docker-compose up -d
```

После успешного развертывания приложение будет доступно по адресу:

[http://localhost:8080/](http://localhost:8080/)

## 📚 Swagger-документация

Для просмотра и проверки работоспособности REST API доступен интерфейс Swagger UI:

[Swagger UI](http://localhost:8080/swagger-ui/index.html)

## 📐 ER-Диаграмма базы данных

Структура базы данных представлена в виде **ER-диаграммы**:

[dbdiagram.io](https://dbdiagram.io/d/67e462b44f7afba18458c26f)

## 📊 Метрики

### Grafana
- Для просмотра работоспособности приложения и проверки логов необходимо перейти в _[Grafana](http://localhost:3000)_

### Prometheus
- Для проверки срабатывания alerts необходимо перейти в _[Prometheus](http://localhost:9090)_

### Actuator
- Список всех доступных метрик через Actuator: _[Метрики](http://localhost:8080/actuator/metrics)_

- Пример конкретной метрики через Actuator: _[Максимальное число активных сессий Tomcat](http://localhost:8080/actuator/metrics/tomcat.sessions.active.max)_

## ❌ Остановка приложения

```bash
docker-compose down
```
