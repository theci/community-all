# DDD3 - Community Platform

DDD2를 기반으로 한 모노레포 구조의 커뮤니티 플랫폼입니다.

## 프로젝트 구조

```
DDD3/
├── backend/              # Spring Boot API 서버
├── apps/
│   ├── web/             # 메인 웹 애플리케이션 (포트 3000)
│   ├── admin/           # 관리자 페이지 (포트 3002)
│   └── mobile-app/      # React Native 모바일 앱
├── packages/
│   └── shared/          # 공유 유틸리티 및 타입
└── docker-compose.yml
```

## 시작하기

### 전체 서비스 실행 (Docker)

```bash
docker-compose up -d
```

### 개별 서비스 실행

**백엔드:**
```bash
cd backend
./gradlew bootRun
```

**웹 (포트 3000):**
```bash
pnpm install
pnpm dev:web
```

**관리자 (포트 3002):**
```bash
pnpm dev:admin
```

## 서비스 포트

- Backend API: `http://3.39.36.234:8080`
- Web Frontend: `http://3.39.36.234:3000`
- Admin Frontend: `http://3.39.36.234:3002`

## 주요 기능

- 게시글 CRUD
- 댓글 시스템
- 좋아요/스크랩
- 포인트 시스템
- 알림 기능
- 파일 업로드 (S3)
- 관리자 콘텐츠/사용자 관리
- 신고 시스템
- 사용자 제재

## 기술 스택

**Backend:**
- Spring Boot 3.2
- Spring Security + JWT
- JPA/Hibernate
- H2 Database (개발), PostgreSQL (프로덕션)
- Redis (AWS ElastiCache)
- AWS S3

**Frontend:**
- Next.js 15
- React 19
- TypeScript
- Tailwind CSS
- Zustand (상태 관리)
- Axios

**DevOps:**
- Docker & Docker Compose
- pnpm workspace (모노레포)
