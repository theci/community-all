# 멀티스테이지 빌드를 사용한 Spring Boot Dockerfile

# 1단계: 빌드 스테이지
FROM gradle:8.5-jdk17-alpine AS build

WORKDIR /app

# Gradle 파일들을 먼저 복사하여 의존성 캐싱 최적화
COPY build.gradle settings.gradle ./

# Gradle wrapper 초기화
RUN gradle wrapper --gradle-version 8.5 --no-daemon

# 의존성 다운로드 (캐시 최적화)
RUN ./gradlew dependencies --no-daemon || true

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드 (테스트 제외)
RUN ./gradlew bootJar --no-daemon -x test

# 2단계: 런타임 스테이지  
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="community-platform"
LABEL description="Community Platform API Server"

# 애플리케이션 사용자 생성 (보안)
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser

WORKDIR /app

# 빌드 스테이지에서 jar 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 설정 파일들 복사 (선택적)
COPY --from=build /app/src/main/resources/application.yml ./

# 파일 권한 설정
RUN chown -R appuser:appgroup /app

# 실행 사용자 변경
USER appuser

# 포트 노출
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM 옵션 설정
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Dspring.profiles.active=docker"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]