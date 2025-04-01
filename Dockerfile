# Используем полный образ JDK 21
FROM openjdk:21-jdk as builder

WORKDIR /app

COPY target/CurrencyExchange-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]