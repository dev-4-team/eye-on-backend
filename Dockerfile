FROM amazoncorretto:17-alpine AS builder
WORKDIR /build
COPY . .
RUN ./gradlew bootJar -x test

# 실행 스테이지
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=builder /build/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]