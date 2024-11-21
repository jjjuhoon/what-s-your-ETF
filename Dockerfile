# 1단계: 빌드 단계
FROM gradle:8.3-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 캐시를 활용하기 위해 필요한 파일만 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle gradle

# 의존성 미리 다운로드 (테스트 제외)
RUN gradle build --no-daemon -x test || return 0

# 전체 소스 코드 복사
COPY . .

# 프로젝트 빌드 (Spring Boot 프로젝트의 jar 파일 생성)
RUN ./gradlew bootJar --no-daemon -x test

# 2단계: 실행 단계
FROM openjdk:17-slim

# 환경 변수 설정 (필요 시 PORT 환경 변수 설정)
ENV PORT=8080

# app.jar로 jar 파일 복사
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# 컨테이너 시작 시 실행할 명령어 설정
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Spring Boot 기본 포트 (8080) 노출
EXPOSE 8080