# Community Platform - Frontend

Next.js 15 + TypeScript 기반 프론트엔드

## 📁 프로젝트 구조

```
frontend/
├── app/                    # Next.js App Router
│   ├── layout.tsx         # 루트 레이아웃
│   ├── page.tsx           # 홈페이지
│   └── globals.css        # 글로벌 스타일
├── components/            # React 컴포넌트
│   ├── ui/               # 기본 UI 컴포넌트
│   ├── layout/           # 레이아웃 컴포넌트
│   ├── features/         # 기능별 컴포넌트
│   └── shared/           # 공유 컴포넌트
├── lib/                  # 라이브러리 코드
│   ├── types/           # TypeScript 타입 정의
│   ├── services/        # API 서비스 레이어
│   ├── hooks/           # 커스텀 훅 (SWR)
│   ├── store/           # Zustand 상태 관리
│   ├── utils/           # 유틸리티 함수
│   └── api.ts           # Axios 설정
├── public/              # 정적 파일
├── .env.local.example   # 환경 변수 예제
├── Dockerfile           # Docker 설정
└── FRONTEND_DEVELOPMENT_GUIDE.md  # 개발 가이드
```

## 🚀 빠른 시작

### 개발 서버 실행

```bash
# 의존성 설치
npm install

# 환경 변수 설정
cp .env.local.example .env.local

# 개발 서버 시작
npm run dev
```

브라우저에서 http://localhost:3000 접속

### Docker로 실행

```bash
# 루트 디렉토리에서
cd ..
docker-compose up community-frontend
```

## 🛠️ 기술 스택

- **Framework**: Next.js 15 (App Router)
- **Language**: TypeScript 5
- **Styling**: Tailwind CSS
- **State Management**: Zustand
- **Data Fetching**: SWR + Axios
- **UI Components**: Custom components

## ✅ 구현 완료

### 인프라
- [x] Next.js 15 + TypeScript 설정
- [x] Tailwind CSS 설정
- [x] Docker 설정

### 타입 시스템
- [x] User, Auth 타입
- [x] Post, Category, Tag 타입
- [x] Comment 타입
- [x] Engagement (Like, Scrap) 타입
- [x] Point, Ranking 타입

### API 레이어
- [x] authService - 인증
- [x] postService - 게시글
- [x] commentService - 댓글
- [x] scrapService - 스크랩
- [x] pointService - 포인트

### 상태 관리
- [x] authStore - 인증 상태
- [x] uiStore - UI 상태

### 커스텀 훅
- [x] useAuth
- [x] usePosts, usePost
- [x] useComments
- [x] useScraps
- [x] usePoints

### UI 컴포넌트
- [x] Button
- [x] Input, Textarea
- [x] Card
- [x] Modal
- [x] Loading

## 📝 개발 스크립트

```bash
npm run dev       # 개발 서버
npm run build     # 프로덕션 빌드
npm run start     # 프로덕션 서버
npm run lint      # ESLint 실행
```

## 🎯 다음 단계

### Phase 2: 인증 구현
- [ ] 로그인 페이지
- [ ] 회원가입 페이지
- [ ] 인증 미들웨어

### Phase 3: 게시판 구현
- [ ] 게시글 목록
- [ ] 게시글 상세
- [ ] 게시글 작성

상세 로드맵: `FRONTEND_DEVELOPMENT_GUIDE.md` 참조

## 📖 문서

- [상세 개발 가이드](./FRONTEND_DEVELOPMENT_GUIDE.md)
- [백엔드 API 문서](../backend/DOCKER_TEST_GUIDE.md)

## 🔧 환경 변수

```bash
# .env.local
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```

## 📄 라이선스

Private Project
