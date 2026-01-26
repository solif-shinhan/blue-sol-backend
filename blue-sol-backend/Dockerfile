# Stage 1: Build
FROM gradle:8.11.1-jdk17 AS builder

WORKDIR /app

# Gradle 의존성 캐싱을 위해 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY . .
RUN gradle clean build -x test --no-daemon

# Stage 2: Run
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]