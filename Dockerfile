# JDK로 빌드
FROM eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app
COPY . .
RUN ./gradlew clean build --no-daemon

# JRE로 실행
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
