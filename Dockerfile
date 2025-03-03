# 베이스 이미지 설정
FROM openjdk:17 as base

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 파일 복사
COPY build/libs/*.jar app.jar

# 포트 설정
EXPOSE 8080

# 애플리케이션 실행
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=dev", "--debug"]