# @ddd3/api-contract

API 클라이언트 및 OpenAPI 기반 자동 생성 서비스

## 현재 상태

✅ **axios 기반 API 클라이언트** (`client.ts`)
- Bearer 토큰 자동 추가
- 401 에러 시 자동 로그아웃
- `ApiResponse<T>` 타입 래퍼

⏸️ **OpenAPI 자동 생성** (백엔드 설정 필요)

## OpenAPI 자동 생성 설정 (향후)

### 1. 백엔드에 Springdoc 추가

`backend/build.gradle`에 추가:
```gradle
dependencies {
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2'
}
```

`application.yml`에 설정:
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

### 2. OpenAPI JSON 확인

```bash
curl http://localhost:8080/v3/api-docs > openapi.json
```

### 3. TypeScript 클라이언트 자동 생성

```bash
cd packages/api-contract
pnpm run generate
```

생성된 파일들:
- `src/generated/services/` - API 서비스 클래스
- `src/generated/models/` - 타입 정의

### 4. 사용 예시

```typescript
// 자동 생성 후
import { PostsService } from '@ddd3/api-contract/generated';

const posts = await PostsService.getPosts({ page: 0, size: 20 });
```

## 현재 사용 방법

```typescript
import { apiClient, ApiResponse } from '@ddd3/api-contract';

const response = await apiClient.get<ApiResponse<Post[]>>('/posts');
const posts = response.data.data;
```
