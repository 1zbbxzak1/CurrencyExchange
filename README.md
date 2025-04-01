## Конвертер валют

Позволяет перевести из одной валюты в другую.
Хранит историю переводов.

## Стек технологий

- **Язык**: Java 21
- **Сборщик проекта**: Maven
- **Фреймворк**: Spring Boot 3.4.3
- **База данных**: PostgreSQL 17

## ER-Диаграмма

[ER-Диаграмма](https://dbdiagram.io/d/67e462b44f7afba18458c26f)

# Установка

```bash
git clone https://github.com/1zbbxzak1/CurrencyExchange.git
```

## Запуск проекта

### Сборка

```bash
mvn clean install
```

### Запуск приложения

```bash
docker-compose up -d
```

После успешного развертывания приложение будет доступно по адресу:

[Swagger UI](http://localhost:8080/swagger-ui)

### Actuator

- Список всех доступных метрик:  
  [Метрики](http://localhost:8080/actuator/metrics)

- Пример конкретной метрики:  
  [Максимальное число активных сессий Tomcat](http://localhost:8080/actuator/metrics/tomcat.sessions.active.max)

## Остановка приложения

```bash
docker-compose down
```