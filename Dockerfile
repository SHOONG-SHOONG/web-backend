# Build
FROM eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app
COPY . .

# gradlew 실행 권한 부여
RUN chmod +x ./gradlew

# 테스트 생략하고 build
RUN ./gradlew clean build -x test --no-daemon

# Runtime 단계 (JRE만 포함)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
