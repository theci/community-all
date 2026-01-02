#!/bin/bash

# Community Platform API Server 시작 스크립트

echo "🚀 Community Platform API Server 시작 중..."

# 기존 컨테이너 정리
echo "📦 기존 컨테이너 정리 중..."
docker-compose down

# 이미지 빌드 및 컨테이너 시작
echo "🔨 Docker 이미지 빌드 및 컨테이너 시작 중..."
docker-compose up --build -d

# 컨테이너 상태 확인
echo "⏰ 서버 시작 대기 중 (최대 60초)..."
for i in {1..60}
do
  if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ 서버가 성공적으로 시작되었습니다!"
    echo ""
    echo "📋 API 서버 정보:"
    echo "   🌐 API Base URL: http://localhost:8080/api/v1"
    echo "   🗂️  H2 Console: http://localhost:8080/h2-console"
    echo "   ❤️  Health Check: http://localhost:8080/actuator/health"
    echo ""
    echo "🧪 Postman으로 테스트하기:"
    echo "   1. Postman에서 postman/Community-Platform-API.postman_collection.json 임포트"
    echo "   2. postman/Local-Environment.postman_environment.json 환경 설정 임포트"
    echo "   3. 1.1 회원가입 → 1.2 로그인 순서로 테스트"
    echo ""
    echo "📊 컨테이너 상태:"
    docker-compose ps
    exit 0
  fi
  echo "⏳ 서버 시작 확인 중... ($i/60)"
  sleep 1
done

echo "❌ 서버 시작 실패! 로그를 확인하세요:"
echo "docker-compose logs community-api"
exit 1