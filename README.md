# DDD3 Community Platform

🎯 최종 아키텍처

  ┌─────────────────────────────────────────────────────┐
  │                    사용자 접속                        │
  └──────────────────┬──────────────────────────────────┘
                     │
           ┌─────────┴─────────┐
           │                   │
      ┌────▼────┐         ┌────▼────┐
      │   PC    │         │  모바일  │
      └────┬────┘         └────┬────┘
           │                   │
      www. domain          m. domain
           │                   │
      ┌────▼────┐         ┌────▼────┐
      │  web    │         │mobile-app│
      │:3000    │         │  :3001   │
      └─────────┘         └──────────┘
           │                   │
           └────────┬──────────┘
                    │
             ┌──────▼──────┐
             │   backend   │
             │    :8080    │
             └─────────────┘

  🌐 도메인 라우팅 로직

  | 디바이스    | 접속 도메인 | 결과                 |
  |-------------|-------------|----------------------|
  | PC          | www.        | ✅ web 앱 표시       |
  | PC          | m.          | 🔄 www.로 리다이렉트 |
  | Mobile      | www.        | 🔄 m.으로 리다이렉트 |
  | Mobile      | m.          | ✅ mobile-app 표시   |
  | App WebView | 모든 도메인 | ✅ 리다이렉트 없음   |

  📦 완성된 패키지 시스템

  packages/
  ├── native-bridge/     ✅ WebView ↔ RN 통신
  ├── shared-hooks/      ✅ 공통 React Hook
  ├── mobile-ui/         ✅ 모바일 UI 컴포넌트
  ├── design-system/     ✅ 공통 UI 컴포넌트
  ├── api-contract/      ✅ API 클라이언트
  └── types/             ✅ 공유 타입

  🚀 실행 방법

  # 1. 의존성 설치 (이미 완료)
  pnpm install

  # 2. 개발 서버 실행
  pnpm dev              # 모든 앱 동시 실행
  # 또는
  pnpm dev:web          # PC 웹만
  pnpm dev:mobile       # 모바일 웹만

  # 3. Docker로 전체 스택 실행
  docker-compose up -d

  # 4. 접속
  # - PC 웹: http://localhost:3000
  # - 모바일 웹: http://localhost:3001
  # - 관리자: http://localhost:3002
  # - 백엔드: http://localhost:8080

  📊 진행 현황

  - ✅ Phase 1: 기반 패키지 구축 (native-bridge, shared-hooks, mobile-ui)
  - ✅ Phase 2: mobile-app 리팩토링 (middleware, 하단 탭)
  - ✅ Phase 3: web 앱 PC 최적화 (middleware, docker-compose, 문서)
  - ⏳ Phase 4: expo-app 생성 (React Native)
  - ⏳ Phase 5: 통합 테스트

  Phase 3가 완료되었습니다! 🎉 다음 Phase 4 (expo-app 생성)를 진행하시겠습니까?

하이브리드 아키텍처 기반 커뮤니티 플랫폼 - DDD(Domain-Driven Design) 프로덕션급 애플리케이션

## 📋 프로젝트 구조

```
/DDD3
├── apps/
│   ├── web/              # PC 웹 (포트 3000, www.domain.com)
│   ├── mobile-app/       # 모바일 웹 (포트 3001, m.domain.com)
│   └── admin/            # 관리자 (포트 3002, admin.domain.com)
├── packages/
│   ├── design-system/    # 공통 UI 컴포넌트
│   ├── api-contract/     # API 클라이언트 (OpenAPI)
│   ├── types/            # 공유 타입 정의
│   ├── native-bridge/    # WebView ↔ React Native 통신
│   ├── shared-hooks/     # 공통 React Hook
│   └── mobile-ui/        # 모바일 전용 UI 컴포넌트
└── backend/              # Spring Boot 3.2 API
```

## 🎯 앱별 역할

| 앱 | 도메인 | 포트 | 목적 | 주요 기능 |
|----|--------|------|------|-----------|
| **web** | www.community.com | 3000 | PC 웹 | 풀 기능, 복잡한 UI, 사이드바 |
| **mobile-app** | m.community.com | 3001 | 모바일 웹 | 간소화 UI, 터치 최적화, 하단 탭 |
| **admin** | admin.community.com | 3002 | 관리자 | 콘텐츠/사용자 관리 |

## 🚀 시작하기

### 필수 요구사항

- Node.js 18+
- pnpm 8+
- Java 17+
- Docker (선택사항)

### 설치

```bash
# 의존성 설치
pnpm install

# 백엔드 빌드 (선택)
cd backend
./gradlew build
```

### 개발 서버 실행

```bash
# 모든 앱 동시 실행 (web, mobile-app, admin)
pnpm dev

# 개별 앱 실행
pnpm dev:web        # PC 웹 (포트 3000)
pnpm dev:mobile     # 모바일 웹 (포트 3001)
pnpm dev:admin      # 관리자 (포트 3002)

# 백엔드 실행
cd backend
./gradlew bootRun
```

### 빌드

```bash
# 모든 앱 빌드
pnpm build

# 개별 앱 빌드
pnpm build:web
pnpm build:mobile
pnpm build:admin
```

### Docker 배포

```bash
# 전체 스택 실행
docker-compose up -d

# 개별 서비스 실행
docker-compose up backend web mobile-app admin
```

## 🌐 디바이스별 라우팅

### 자동 리다이렉트

- **PC에서 www. 접속** → ✅ 정상 (web 앱)
- **PC에서 m. 접속** → 🔄 www.로 리다이렉트
- **모바일에서 m. 접속** → ✅ 정상 (mobile-app)
- **모바일에서 www. 접속** → 🔄 m.으로 리다이렉트
- **앱 WebView에서 접속** → ✅ 리다이렉트 없음

### Middleware 구현

각 앱의 `middleware.ts`가 User-Agent를 감지하여 자동으로 적절한 도메인으로 리다이렉트합니다.

## 📦 패키지 시스템

### @ddd3/native-bridge

WebView와 React Native 간 통신을 위한 브릿지

```typescript
import { WebViewBridge, DeviceDetector } from '@ddd3/native-bridge';

// WebView → React Native 메시지 전송
WebViewBridge.sendAuthToken(token);

// 디바이스 감지
const { isInApp, isMobile } = DeviceDetector.getDeviceInfo();
```

### @ddd3/shared-hooks

공통 React Hook

```typescript
import { useAppDetection, useWebViewBridge } from '@ddd3/shared-hooks';

// 앱 환경 감지
const { isInApp, platform } = useAppDetection();

// WebView 통신
const { postMessage } = useWebViewBridge('AUTH_LOGIN', handleLogin);
```

### @ddd3/mobile-ui

모바일 전용 UI 컴포넌트

```typescript
import { BottomTabBar, MobileHeader } from '@ddd3/mobile-ui';

<BottomTabBar tabs={[
  { label: '홈', icon: <HomeIcon />, href: '/' },
  { label: '게시글', icon: <PostIcon />, href: '/posts' }
]} />
```

## 🔧 기술 스택

### Frontend
- **Framework**: Next.js 15 (App Router)
- **Language**: TypeScript 5
- **Styling**: Tailwind CSS 3
- **State**: Zustand, SWR
- **Package Manager**: pnpm (Workspace)

### Backend
- **Framework**: Spring Boot 3.2
- **Language**: Java 17
- **Database**: H2 (dev), PostgreSQL (prod)
- **Architecture**: DDD, Clean Architecture

### DevOps
- **Container**: Docker, Docker Compose
- **CI/CD**: GitHub Actions (예정)
- **Monitoring**: Sentry (예정)

## 📝 개발 가이드

### 새 패키지 추가

```bash
mkdir -p packages/your-package/src
cd packages/your-package
pnpm init
```

`pnpm-workspace.yaml`에 패키지 추가:
```yaml
packages:
  - 'packages/your-package'
```

### API 재생성

```bash
cd packages/api-contract
pnpm generate
```

### 타입 체크

```bash
pnpm --filter @ddd3/web exec tsc --noEmit
```

## 🧪 테스트

```bash
# 전체 테스트
pnpm test

# 개별 앱 테스트
pnpm --filter @ddd3/web test
```

## 📊 프로젝트 현황

### Phase 1: 기반 패키지 구축 ✅
- ✅ @ddd3/native-bridge
- ✅ @ddd3/shared-hooks
- ✅ @ddd3/mobile-ui

### Phase 2: mobile-app 리팩토링 ✅
- ✅ middleware.ts
- ✅ 모바일 최적화 layout
- ✅ 하단 탭 네비게이션

### Phase 3: web 앱 PC 최적화 ✅
- ✅ middleware.ts
- ✅ PC 최적화 metadata
- ✅ Docker Compose 업데이트

### Phase 4: expo-app 생성 (예정)
- ⏳ React Native (Expo)
- ⏳ WebView 통합
- ⏳ 푸시 알림

### Phase 5: 통합 테스트 (예정)
- ⏳ E2E 테스트
- ⏳ 성능 최적화
- ⏳ 배포 자동화

## 🤝 기여하기

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 라이선스

This project is licensed under the MIT License.

## 🔗 관련 링크

- [API 문서](http://localhost:8080/swagger-ui.html)
- [프로젝트 계획서](./HYBRID_APP_IMPLEMENTATION_PLAN.md)
- [이슈 트래커](https://github.com/your-repo/issues)

## 📞 연락처

- 이메일: team@ddd3.com
- Slack: #ddd3-platform
