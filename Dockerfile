# Этап 1: Сборка приложения
FROM openjdk:21-jdk as builder

# Устанавливаем рабочую директорию
WORKDIR /build

# Копируем JAR файл приложения
COPY target/CurrencyExchange-0.0.1-SNAPSHOT.jar app.jar

# Этап 2: Создание финального образа
FROM openjdk:21-slim

# Метаданные образа
LABEL maintainer="Currency Exchange Team"
LABEL description="Currency Exchange Service Container"

# Установка wget для healthcheck
RUN apt-get update && \
    apt-get install -y wget && \
    rm -rf /var/lib/apt/lists/*

# Создаем пользователя без прав root для безопасности
RUN groupadd -r spring && useradd -r -g spring spring

# Устанавливаем рабочую директорию
WORKDIR /app

# Создаем директорию для логов и устанавливаем права
RUN mkdir -p /app/logs && \
    chown -R spring:spring /app

# Копируем JAR из этапа сборки
COPY --from=builder /build/app.jar app.jar

# Назначаем владельца файлов
RUN chown spring:spring /app/app.jar

# Переключаемся на пользователя без прав root
USER spring

# Объявляем порт, который будет использовать приложение
EXPOSE 8080

# Настраиваем переменные среды Java
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Запускаем приложение
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]