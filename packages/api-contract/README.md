# @ddd3/api-contract

API 클라이언트 및 OpenAPI 기반 자동 생성 서비스

## 현재 상태

✅ **axios 기반 API 클라이언트** (`client.ts`)
- Bearer 토큰 자동 추가
- 401 에러 시 자동 로그아웃
- `ApiResponse<T>` 타입 래퍼

✅ **OpenAPI 자동 생성** - Springdoc 설정 완료
- 백엔드: Springdoc OpenAPI 2.3.0
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/v3/api-docs

## 왜 OpenAPI 자동 생성이 필요한가?

### 문제 상황: 수동 API 호출의 한계

#### 1. 타입 안정성 부재
```typescript
// ❌ 수동 방식 - 타입 불일치 위험
const response = await apiClient.get('/api/v1/posts');
const posts = response.data.data; // any 타입, 런타임 에러 가능성

// 백엔드가 응답 구조를 변경하면?
// { data: { posts: [...] } } → { data: { content: [...] } }
// 프론트엔드는 컴파일 에러 없이 런타임에서 터짐
```

#### 2. API 파라미터 오류
```typescript
// ❌ 수동 방식 - 오타나 잘못된 파라미터
await apiClient.get('/api/v1/posts', {
  params: {
    pge: 0,        // 오타: page → pge
    szie: 20,      // 오타: size → szie
    srot: 'name'   // 오타: sort → srot
  }
});
// IDE가 오류를 찾아주지 못하고, 백엔드는 기본값으로 동작
// 디버깅에 시간 소모
```

#### 3. 백엔드 변경 시 수동 동기화
```typescript
// 백엔드에서 새로운 필드 추가: Post.viewCount
// 프론트엔드 개발자는 어떻게 알 수 있을까?
// - Slack 메시지를 확인? → 놓칠 수 있음
// - API 문서를 확인? → 문서가 최신인지 불확실
// - 백엔드 코드를 직접 확인? → 비효율적

// 수동으로 타입 정의 추가 필요
interface Post {
  id: number;
  title: string;
  content: string;
  viewCount: number; // ← 수동으로 추가
}
```

#### 4. 중복된 타입 정의
```typescript
// ❌ 프론트엔드에서 중복 타입 정의
// packages/types/src/post.ts
export interface Post {
  id: number;
  title: string;
  // ...
}

// 백엔드에도 같은 타입이 있음
// backend/src/main/java/...domain/Post.java
public class Post {
  private Long id;
  private String title;
  // ...
}

// 두 개의 진실 공급원(Source of Truth) → 불일치 위험
```

### 해결책: OpenAPI 자동 생성

#### 1. 완벽한 타입 안정성
```typescript
// ✅ 자동 생성 방식 - 완벽한 타입 추론
import { PostControllerService } from '@ddd3/api-contract/generated';

const response = await PostControllerService.getPosts({
  page: 0,
  size: 20,
  sort: 'createdAt,desc'
});

// response.data는 ApiResponsePageResponsePostSummaryResponse 타입
// posts는 PostSummaryResponse[] 타입
const posts = response.data.content;

// 백엔드가 응답 구조를 변경하면?
// → 컴파일 에러로 즉시 감지!
// Property 'data' does not exist on type 'ApiResponsePageResponsePostSummaryResponse'
```

#### 2. IDE 자동완성 및 타입 힌트
```typescript
// ✅ 파라미터 오류 방지
await PostControllerService.getPosts({
  page: 0,
  size: 20,
  sort: 'createdAt,desc'
  // pge를 입력하면? → IDE가 즉시 빨간줄 표시
  // Ctrl+Space로 사용 가능한 파라미터 목록 확인 가능
});
```

#### 3. 백엔드 변경 자동 반영
```bash
# 백엔드 개발자가 Post에 viewCount 추가
# 1. OpenAPI spec 자동 업데이트됨
# 2. 프론트엔드에서 클라이언트 재생성
pnpm run generate

# 3. PostSummaryResponse 타입에 viewCount가 자동 추가됨
# 4. 기존 코드에서 viewCount 사용 가능
console.log(post.viewCount); // ← IDE가 자동완성 제공
```

#### 4. 단일 진실 공급원 (Single Source of Truth)
```
백엔드 Java 코드
    ↓
Springdoc이 자동으로 OpenAPI Spec 생성
    ↓
openapi-typescript-codegen이 TypeScript 타입 생성
    ↓
프론트엔드에서 사용

→ 백엔드 코드가 유일한 진실 공급원
→ 타입 불일치 원천 차단
```

### 실제 개발 시나리오

#### 시나리오 1: 새로운 API 추가
```typescript
// 백엔드: GET /api/v1/posts/{postId}/comments 추가

// 수동 방식 (AS-IS)
// 1. 백엔드 개발자가 Slack에 API 추가 공지 (5분)
// 2. 프론트엔드 개발자가 메시지 확인 (놓칠 수 있음)
// 3. 타입 정의 작성 (10분)
// 4. API 클라이언트 함수 작성 (10분)
// 5. 테스트 (5분)
// → 총 30분 + 커뮤니케이션 오버헤드

// 자동 생성 방식 (TO-BE)
// 1. pnpm run generate (10초)
// 2. 바로 사용 가능
import { CommentControllerService } from '@ddd3/api-contract/generated';
const comments = await CommentControllerService.getComments({ postId: 1 });
// → 총 1분
```

#### 시나리오 2: API 응답 구조 변경
```typescript
// 백엔드: PostResponse에 새 필드 추가
// - author: UserSummaryResponse
// - tags: string[]

// 수동 방식 (AS-IS)
// 1. 런타임에 undefined 에러 발생 후 발견
// 2. 백엔드 개발자에게 문의 (30분)
// 3. 타입 정의 수정 (5분)
// → 디버깅 시간 낭비, 배포 후 발견 시 장애

// 자동 생성 방식 (TO-BE)
// 1. pnpm run generate
// 2. 컴파일 에러로 즉시 발견
// 3. IDE가 새 필드 자동완성 제공
// → 배포 전 컴파일 타임에 모든 문제 해결
```

#### 시나리오 3: 여러 프론트엔드 앱 유지보수
```
프로젝트 구조:
- apps/web (사용자용)
- apps/admin (관리자용)
- apps/mobile-app (모바일용)

// 수동 방식 (AS-IS)
// API 변경 시 3개 앱 모두 수동 수정
// → 실수로 한 곳을 빠트릴 위험
// → 각 앱마다 중복된 타입 정의

// 자동 생성 방식 (TO-BE)
// @ddd3/api-contract 패키지 하나만 재생성
// → 3개 앱 모두 자동으로 최신 API 타입 사용
// → DRY 원칙 준수
```

### 비용 대비 효과

#### 초기 설정 비용
- Springdoc 설정: 10분
- 자동 생성 스크립트 설정: 10분
- **총 20분**

#### 장기적 이득
- API 변경 시 프론트엔드 동기화: **30분 → 1분** (매 변경마다)
- 타입 불일치로 인한 버그: **0건** (컴파일 타임 방지)
- API 문서 유지보수: **자동화** (Swagger UI)
- 새 팀원 온보딩: **빠름** (타입 힌트와 자동완성)

#### ROI (투자 대비 수익)
```
월 평균 API 변경 횟수: 20회
절약 시간: 29분 × 20회 = 580분 (약 10시간/월)
연간 절약: 120시간

+ 버그 감소로 인한 디버깅 시간 절약
+ 더 나은 개발자 경험 (DX)
+ 코드 품질 향상
```

## OpenAPI 자동 생성 사용법

### 1. 백엔드 서버 실행

Docker Compose로 백엔드 실행:
```bash
docker-compose up -d backend
```

또는 Gradle로 직접 실행:
```bash
cd backend
./gradlew bootRun
```

### 2. OpenAPI Spec 확인

Swagger UI에서 확인:
```
http://localhost:8080/swagger-ui.html
```

JSON으로 다운로드:
```bash
curl http://localhost:8080/v3/api-docs > openapi.json
```

### 3. TypeScript 클라이언트 자동 생성

백엔드가 실행 중인 상태에서:
```bash
cd packages/api-contract
pnpm run generate
```

생성된 파일들:
- `src/generated/services/` - 15개 API 서비스 클래스
  - AdminControllerService
  - AuthControllerService
  - PostControllerService
  - CommentControllerService
  - UserControllerService
  - 등...
- `src/generated/models/` - 타입 정의 (100개 이상)
- `src/generated/index.ts` - 통합 export

### 4. 생성된 클라이언트 사용 예시

```typescript
// 자동 생성된 서비스 사용
import { PostControllerService, UserControllerService } from '@ddd3/api-contract/generated';

// 게시글 목록 조회
const postsPage = await PostControllerService.getPosts({
  page: 0,
  size: 20,
  sort: 'createdAt,desc'
});

// 특정 게시글 조회
const post = await PostControllerService.getPost({
  postId: 1,
  currentUserId: 123
});

// 사용자 정보 조회
const user = await UserControllerService.getUser({
  userId: 123
});
```

## 수동 API 클라이언트 사용 (기존 방법)

자동 생성 대신 수동으로 API 호출:
```typescript
import { apiClient, ApiResponse } from '@ddd3/api-contract';

const response = await apiClient.get<ApiResponse<Post[]>>('/posts');
const posts = response.data.data;
```

## 백엔드 OpenAPI 설정 상세

`backend/build.gradle`:
```gradle
dependencies {
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
}
```

`backend/src/main/resources/application.yml`:
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
```
