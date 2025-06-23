#!/bin/bash

# Остановка контейнеров
docker-compose down -v

# Сборка приложения
./mvnw clean package -DskipTests

# Пересборка и запуск контейнеров
docker-compose up --build -d

# Вывод логов
docker-compose logs -f 