#!/bin/bash

# Community Platform API Server 중지 스크립트

echo "🛑 Community Platform API Server 중지 중..."

# 컨테이너 중지 및 제거
docker-compose down

# 이미지 정리 (선택적)
read -p "🗑️  Docker 이미지도 삭제하시겠습니까? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
    echo "🧹 Docker 이미지 정리 중..."
    docker-compose down --rmi local
    docker system prune -f
fi

echo "✅ 서버가 성공적으로 중지되었습니다!"