# --- ЭТАП 1: Сборка приложения ---
# Используем официальный образ Maven с Java 21 для компиляции
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

# Копируем pom.xml и скачиваем зависимости (это ускорит последующие сборки)
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем исходный код и запускаем сборку
COPY src ./src
# Флаг -DskipTests пропускает тесты при сборке образа (тесты лучше запускать локально)
RUN mvn clean package -DskipTests

# --- ЭТАП 2: Запуск приложения ---
# Используем легковесный образ только с JRE (Java Runtime Environment)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем готовый jar-файл из первого этапа
COPY --from=build /app/target/*.jar app.jar

# Указываем порт, который будет слушать контейнер
EXPOSE 8080

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]