# ---------- STAGE 1: Build ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY . .

ARG SERVICE

# Собираем конкретный модуль, используя родителя
RUN mvn -f pom.xml -pl ${SERVICE} -am clean package -DskipTests


# ---------- STAGE 2: Runtime ----------
FROM eclipse-temurin:21-jdk
WORKDIR /app

ARG SERVICE
COPY --from=build /build/${SERVICE}/target/*.jar app.jar

ARG PORT
EXPOSE ${PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
