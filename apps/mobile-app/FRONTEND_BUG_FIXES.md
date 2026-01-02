# Frontend 에러 수정 기록

Next.js 프로젝트에서 발생한 에러와 해결 방법을 정리한 문서입니다.

---

## 1. 게시글 작성 시 "사용자 정보를 불러올 수 없습니다" 에러

### 문제
- 로그인 후 게시글 작성 시 "사용자 정보를 불러올 수 없습니다" 에러 발생
- `user` 객체가 `null`로 나타남

### 원인
백엔드 API 응답 형식과 프론트엔드 파싱 불일치

**백엔드 실제 응답**:
```json
{
  "success": true,
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "user": { "id": 1, "email": "...", ... }
  }
}
```

**프론트엔드 코드**:
```typescript
// authService.ts (잘못된 코드)
const response = await apiClient.post('/auth/login', credentials);
return response.data; // { success, data, timestamp } 전체를 반환
```

이렇게 하면 `response.data`는 `{ success: true, data: {...} }` 전체이고,
실제 user 정보는 `response.data.data.user`에 있음

### 해결
`response.data.data`로 접근하도록 수정

```typescript
// lib/services/authService.ts
interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
}

export const authService = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', credentials);
    return response.data.data; // ✅ 올바른 접근
  },
};
```

### 학습 포인트
- API 응답 구조를 정확히 파악해야 함
- 타입스크립트 제네릭을 사용해 API 래핑 구조 표현
- 다른 서비스(`postService.ts`)의 패턴을 참고하면 도움됨

---

## 2. 작성한 게시글이 목록에 나타나지 않는 문제

### 문제
- 게시글 작성 후 상세 페이지는 정상 조회됨
- 하지만 게시글 목록(`/posts`)에는 나타나지 않음
- API 응답: `content: []`, `totalElements: 0`

### 원인
백엔드 API 구조 이해 부족

**백엔드 동작 방식**:
1. `POST /api/v1/posts` → 게시글 생성 (항상 **DRAFT** 상태)
2. `POST /api/v1/posts/{postId}/publish` → 게시글 발행 (DRAFT → **PUBLISHED**)
3. `GET /api/v1/posts` → **발행된 게시글만** 조회

**프론트엔드 잘못된 구현**:
```typescript
// 게시글 작성만 하고 publish API 호출 안 함
const createdPost = await postService.createPost(postData, user.id);
router.push(`/posts/${createdPost.id}`); // DRAFT 상태로 저장됨
```

프론트엔드가 `status: PUBLISHED`를 보내도, 백엔드가 이를 무시하고 DRAFT로 저장

### 해결
"발행" 버튼 클릭 시 publish API 자동 호출

```typescript
// app/(main)/posts/create/page.tsx
const createdPost = await postService.createPost(postData, user.id);

// ✅ 발행 상태일 경우, publish API 호출
if (status === PostStatus.PUBLISHED) {
  await postService.publishPost(createdPost.id, user.id);
  alert('게시글이 발행되었습니다.');
  router.push(`/posts/${createdPost.id}`);
}
```

### 추가 수정: API 응답 구조 불일치

**백엔드 실제 응답**:
```json
{
  "content": [...],
  "pageInfo": {
    "totalPages": 5,
    "totalElements": 100,
    "currentPage": 0
  }
}
```

**프론트엔드 잘못된 코드**:
```typescript
setTotalPages(response.totalPages); // ❌ undefined
```

**수정**:
```typescript
// lib/types/post.ts
export interface PostListResponse {
  content: Post[];
  pageInfo: PageInfo; // ✅ 페이지 정보를 객체로 분리
}

// app/(main)/posts/page.tsx
setTotalPages(response.pageInfo.totalPages); // ✅ 올바른 접근
```

### 학습 포인트
- 백엔드 API 명세를 정확히 이해해야 함
- 게시글 생성과 발행이 분리된 워크플로우
- API 응답 구조가 중첩되어 있을 때 타입 정의 중요

---

## 핵심 교훈

### 1. API 통신 디버깅 방법
```typescript
// 항상 API 응답을 로그로 확인
console.log('API Response:', response);
console.log('Data:', response.data);
```

### 2. 타입스크립트 활용
```typescript
// 제네릭으로 API 래핑 구조 표현
interface ApiResponse<T> {
  success: boolean;
  data: T;
}

// 사용 예시
const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login');
```

### 3. 기존 코드 참고
- 같은 프로젝트 내 다른 서비스 코드 확인
- `postService.ts`는 올바른 패턴 → `authService.ts` 수정 시 참고

### 4. curl로 백엔드 직접 테스트
```bash
# 게시글 목록이 비어있는지 확인
curl "http://3.39.36.234:8080/api/v1/posts?page=0&size=20"
```

---

---

## 3. ReactMarkdown className 에러

### 문제
- `react-markdown` 라이브러리에서 `className` prop 지원 중단
- 에러: "Unexpected `className` prop, remove it"

### 원인
`react-markdown` v9+ 버전에서 `className` prop이 제거됨

### 해결
부모 div에 스타일 적용

```typescript
// Before (에러)
<ReactMarkdown className="text-gray-800">
  {post.content}
</ReactMarkdown>

// After (수정)
<div className="prose prose-lg max-w-none text-gray-800">
  <ReactMarkdown>
    {post.content}
  </ReactMarkdown>
</div>
```

### 학습 포인트
- 라이브러리 업데이트 시 CHANGELOG 확인 필요
- 스타일은 wrapper div로 감싸서 적용

---

## 4. 좋아요/스크랩 상태 조회 시 500 에러

### 문제
- 게시글 상세 페이지에서 좋아요/스크랩 상태 조회 실패
- 에러: `Request failed with status code 500`

### 원인
백엔드 API가 `currentUserId` 쿼리 파라미터를 필수로 요구함

**백엔드 API**:
```
GET /api/v1/posts/{postId}/like/status?currentUserId={userId}
GET /api/v1/posts/{postId}/scrap/status?currentUserId={userId}
```

**프론트엔드 잘못된 코드**:
```typescript
// currentUserId 없이 호출
const status = await postService.getLikeStatus(postId);
```

### 해결
API 호출 시 `currentUserId` 쿼리 파라미터 추가

```typescript
// lib/services/postService.ts
getLikeStatus: async (postId: number, currentUserId?: number) => {
  const params = currentUserId ? { currentUserId } : {};
  const response = await apiClient.get(`/posts/${postId}/like/status`, { params });
  return response.data.data;
}

// app/(main)/posts/[id]/page.tsx
const loadLikeStatus = async () => {
  if (!user?.id) return; // 로그인하지 않은 경우 스킵
  const status = await postService.getLikeStatus(postId, user.id);
  setLiked(status.liked);
};
```

### 학습 포인트
- 백엔드 API 명세에서 필수 파라미터 확인
- axios에서 쿼리 파라미터는 `{ params }` 옵션으로 전달
- 로그인하지 않은 경우 early return으로 불필요한 API 호출 방지

---

## 5. 댓글 계층 구조 시각적 구분 부족

### 문제
- 댓글과 대댓글이 화면상으로 구분되지 않음
- 답글 레벨을 파악하기 어려움
- 깊은 중첩 시 가독성 저하

### 원인
CommentItem 컴포넌트에 depth 기반 스타일링 부재

### 해결
5단계 깊이까지 들여쓰기 및 시각적 차별화 구현

```typescript
// components/features/comment/CommentItem.tsx
interface CommentItemProps {
  depth?: number; // 댓글 깊이 추가
  // ...
}

// 최대 5단계까지 제한, 이후는 5단계 유지
const currentDepth = Math.min(depth, 5);
const nextDepth = Math.min(depth + 1, 5);

// depth별 배경색 차별화
const bgColorClass = currentDepth === 0
  ? 'bg-gray-50'      // 최상위 댓글
  : currentDepth === 1
  ? 'bg-blue-50'      // 1단계 답글
  : currentDepth === 2
  ? 'bg-green-50'     // 2단계 답글
  : currentDepth === 3
  ? 'bg-yellow-50'    // 3단계 답글
  : currentDepth === 4
  ? 'bg-purple-50'    // 4단계 답글
  : 'bg-pink-50';     // 5단계 이상 답글

// 답글 화살표 표시 (레벨 배지는 제거)
{currentDepth > 0 && (
  <span className="text-xs font-semibold text-blue-600">↳</span>
)}

// 재귀적으로 depth 전달
<CommentItem
  comment={reply}
  depth={nextDepth}
  // ...
/>
```

**추가 수정 사항**:
```typescript
// CommentList.tsx - 최상위 댓글에 depth={0} 명시
<CommentItem depth={0} {...props} />

// CommentItem.tsx - 답글 추가 시 자동으로 펼치기
useEffect(() => {
  if (comment.replies && comment.replies.length > 0) {
    setShowReplies(true);
  }
}, [comment.replies]);
```

### 학습 포인트
- 재귀 컴포넌트에서 깊이 추적 중요
- `Math.min(depth, 5)`으로 최대 깊이 제한
- Tailwind CSS 조건부 클래스 활용
- 시각적 계층 구조: 색상 + 아이콘 + 들여쓰기 조합
- useEffect로 답글 자동 펼치기 구현

---

## 6. 게시글 이미지 업로드 문제 (중복 & 깨짐)

### 문제 1: 이미지 1개 업로드 시 2개로 중복
- 이미지 버튼 클릭 후 1개 선택 시 `![굿아이디어.jpg](temp:0)` 가 2번 삽입됨

### 원인
중첩된 상태 업데이트로 인한 React 렌더링 문제

**문제가 있던 코드**:
```typescript
// app/(main)/posts/create/page.tsx
files.forEach((file) => {
  setImageFiles((prev) => {
    const newImageFiles = [...prev, { file, localUrl }];

    // ❌ setImageFiles 안에서 setContent 호출 (중첩된 상태 업데이트)
    setContent((prevContent) => {
      const newContent = prevContent + imageMarkdown;
      return newContent;
    });

    return newImageFiles;
  });
});
```

문제점:
- `setImageFiles` 함수 안에서 `setContent`를 호출하는 중첩 구조
- React 배치 업데이트 시 예상치 못한 동작 발생
- `forEach` 루프에서 각 파일마다 별도 상태 업데이트

### 해결 1: 상태 업데이트 분리

```typescript
// 수정된 코드
const handleImageInsert = () => {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'image/*';
  input.multiple = true;
  input.onchange = (e: Event) => {
    const files = Array.from(target.files || []);
    if (files.length === 0) return;

    // 파일 크기 검증
    const validFiles = files.filter((file) => {
      if (file.size > 10 * 1024 * 1024) {
        alert(`${file.name}: 파일 크기가 너무 큽니다. (최대 10MB)`);
        return false;
      }
      return true;
    });

    // ✅ 1. 커서 위치를 먼저 저장
    const textarea = document.getElementById('content-textarea') as HTMLTextAreaElement;
    const cursorPosition = textarea ? textarea.selectionStart : content.length;

    // ✅ 2. 모든 파일을 한 번에 처리
    const newImageItems = validFiles.map((file) => ({
      file,
      localUrl: URL.createObjectURL(file),
    }));

    const startIndex = imageFiles.length;

    // ✅ 3. 마크다운 텍스트 생성
    const imageMarkdowns = newImageItems
      .map((item, i) => `\n![${item.file.name}](temp:${startIndex + i})\n`)
      .join('');

    // ✅ 4. 상태 업데이트를 분리해서 순차적으로 처리
    setImageFiles((prev) => [...prev, ...newImageItems]);
    setContent((prev) =>
      prev.substring(0, cursorPosition) + imageMarkdowns + prev.substring(cursorPosition)
    );

    // 커서 위치 조정
    if (textarea) {
      setTimeout(() => {
        textarea.focus();
        textarea.setSelectionRange(
          cursorPosition + imageMarkdowns.length,
          cursorPosition + imageMarkdowns.length
        );
      }, 0);
    }
  };
  input.click();
};
```

---

### 문제 2: 발행된 게시글에서 이미지 깨짐 (403/401 에러)

초기 에러:
```
GET http://3.39.36.234:3001/api/v1/files/posts/attachments/.../image.jpg 401 (Unauthorized)
```

이후 에러:
```
GET https://test-airgateway.s3.ap-northeast-2.amazonaws.com/posts/attachments/.../image.jpg 403 (Forbidden)
```

### 원인 분석

**단계 1**: 백엔드 `fileUrl` 필드가 상대 경로 반환
```json
{
  "fileUrl": "/api/v1/files/posts/attachments/2025/12/27/image.jpg",
  "filePath": "posts/attachments/2025/12/27/image.jpg"
}
```

- `fileUrl`을 그대로 사용하면 브라우저가 상대 경로로 인식
- 프론트엔드 서버(3001 포트)로 요청 → 401 Unauthorized

**단계 2**: S3 직접 URL로 변경했지만 여전히 접근 불가
```typescript
const s3Url = `https://test-airgateway.s3.ap-northeast-2.amazonaws.com/${attachment.filePath}`;
```

- S3 버킷이 기본적으로 private 설정
- 공개 읽기 권한 없음 → 403 Forbidden

**단계 3**: 백엔드 API로 프록시 시도
```
GET http://3.39.36.234:8080/api/v1/files/posts/attachments/.../image.jpg
→ 401 Unauthorized (인증 필요)
```

### 해결 2: S3 버킷 공개 설정

**AWS S3 버킷 정책 추가**:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::test-airgateway/posts/attachments/*"
    }
  ]
}
```

**프론트엔드 최종 코드**:
```typescript
// app/(main)/posts/create/page.tsx
if (imageFiles.length > 0) {
  const files = imageFiles.map((img) => img.file);
  const uploadedAttachments = await attachmentService.uploadAttachments(
    createdPost.id,
    files,
    user.id
  );

  // temp: URL을 S3 직접 URL로 교체
  uploadedAttachments.forEach((attachment, index) => {
    const s3Url = `https://test-airgateway.s3.ap-northeast-2.amazonaws.com/${attachment.filePath}`;

    const regex = new RegExp(`temp:${index}`, 'g');
    finalContent = finalContent.replace(regex, s3Url);
  });

  // 게시글 업데이트로 URL 반영
  await postService.updatePost(createdPost.id, {
    title: createdPost.title,
    content: finalContent,
    contentType: 'MARKDOWN',
    categoryId: createdPost.category?.id,
  }, user.id);
}
```

### 학습 포인트

#### 1. React 상태 관리 원칙
- **중첩된 setState 호출 금지**: 한 setState 안에서 다른 setState 호출 X
- **배치 업데이트 이해**: React는 여러 setState를 배치로 처리
- **순차적 상태 업데이트**: 서로 독립적인 상태는 분리해서 업데이트

#### 2. 파일 업로드 워크플로우
```
1. 로컬 미리보기: URL.createObjectURL() → blob:http://...
2. 임시 마크다운: ![image.jpg](temp:0)
3. 서버 업로드: S3에 파일 저장
4. URL 교체: temp:0 → 실제 S3 URL
5. 게시글 업데이트: 최종 content 저장
```

#### 3. S3 파일 접근 방식 비교

| 방식 | URL 예시 | 장점 | 단점 |
|------|----------|------|------|
| **S3 직접** | `https://bucket.s3.region.amazonaws.com/path` | 빠름, CDN 가능 | 공개 설정 필요 |
| **백엔드 프록시** | `http://api.com/api/v1/files/path` | 권한 제어 가능 | 서버 부하, 느림 |
| **Presigned URL** | `https://bucket.s3...?AWSAccessKeyId=...` | 임시 권한 부여 | URL 만료 관리 |

**공개 블로그/포럼**: S3 직접 URL 권장 (성능 최적화)

#### 4. 디버깅 팁
```typescript
// URL 교체 로그 확인
console.log('교체 전:', finalContent);
console.log('교체 대상:', `temp:${index}`);
console.log('교체 후:', finalContent.replace(regex, s3Url));

// 정규식 일치 개수 확인
const matches = (finalContent.match(regex) || []).length;
console.log('일치 개수:', matches); // 0이면 교체 실패
```

#### 5. TypeScript 타입 정의
```typescript
// lib/types/attachment.ts
export interface Attachment {
  id: number;
  postId: number;
  fileName: string;
  originalName: string;    // ⚠️ originalFileName이 아님!
  fileType: AttachmentType;
  filePath: string;         // S3 키 (상대 경로)
  fileUrl: string;          // 백엔드 API 경로 (상대 경로)
  fileSize: number;
  mimeType: string;
  displayOrder: number;
}
```

---

---

## 7. 게시글 미리보기 이미지 에러

### 문제
- 게시글 작성 페이지에서 "미리보기" 버튼 클릭 시 콘솔 에러 발생
- 에러: `An empty string ("") was passed to the src attribute`
- 브라우저가 현재 페이지 전체를 다운로드하려고 시도

### 원인
ReactMarkdown이 `temp:0` 같은 임시 URL이나 빈 문자열을 `<img src="">`로 렌더링

**문제가 있던 코드**:
```typescript
// 마크다운에 temp:0, temp:1 같은 임시 URL 포함
const content = "# 제목\n![이미지](temp:0)\n본문...";

// ReactMarkdown이 그대로 렌더링 시도
<ReactMarkdown>{content}</ReactMarkdown>
// → <img src="temp:0"> 또는 <img src="">로 변환됨
```

### 해결
ReactMarkdown에 커스텀 이미지 컴포넌트 추가

```typescript
// app/(main)/posts/create/page.tsx
<ReactMarkdown
  remarkPlugins={[remarkGfm]}
  components={{
    // ✅ 커스텀 이미지 렌더러: 유효한 URL만 렌더링
    img: ({ node, src, alt, ...props }) => {
      // src가 없거나 빈 문자열이면 렌더링하지 않음
      if (!src || src.trim() === '') {
        return <span className="text-red-500">[이미지 URL 없음: {alt}]</span>;
      }

      // temp: URL이면 로컬 blob URL로 교체해서 미리보기
      if (src.startsWith('temp:')) {
        const tempIndex = parseInt(src.replace('temp:', ''));
        const imageFile = imageFiles[tempIndex];

        if (imageFile?.localUrl) {
          return (
            <img
              src={imageFile.localUrl}
              alt={alt || ''}
              {...props}
              className="max-w-full h-auto"
            />
          );
        } else {
          return <span className="text-yellow-600">[이미지 로딩 중...]</span>;
        }
      }

      // 일반 URL은 그대로 렌더링
      return <img src={src} alt={alt || ''} {...props} className="max-w-full h-auto" />;
    },
  }}
>
  {content}
</ReactMarkdown>
```

### 학습 포인트
- ReactMarkdown은 `components` prop으로 커스텀 렌더러 지정 가능
- `temp:` 접두사를 사용한 임시 URL은 실제 blob URL로 매핑 필요
- 방어적 프로그래밍: src가 유효한지 항상 검증

---

## 8. Phase 5 구현 - 프로필/랭킹/스크랩 페이지 에러

### 문제 1: Profile과 Scraps 페이지 로그인 리다이렉트

**현상**:
- Ranking 페이지는 정상 작동
- Profile, Scraps 페이지는 로그인 페이지로 자동 리다이렉트

### 원인
Zustand persist 하이드레이션이 완료되기 전에 인증 상태 체크

**문제가 있던 코드**:
```typescript
// lib/hooks/useAuth.ts
export function useAuth() {
  const { user, isAuthenticated, isLoading } = useAuthStore();

  return {
    isAuthenticated,  // ❌ localStorage 복원 전에는 false
    isLoading,
    // ...
  };
}

// app/(main)/profile/page.tsx
useEffect(() => {
  if (!authLoading && !isAuthenticated) {
    router.push('/login?redirect=/profile');  // ❌ 하이드레이션 전에 실행됨
  }
}, [authLoading, isAuthenticated]);
```

**동작 순서**:
1. 페이지 로드 → `isAuthenticated = false` (초기값)
2. useEffect 실행 → 로그인 페이지로 리다이렉트
3. localStorage 하이드레이션 완료 → 너무 늦음!

### 해결 1: 하이드레이션 상태 추적

```typescript
// lib/store/authStore.ts
interface AuthState {
  // ... 기존 상태
  hasHydrated: boolean;  // ✅ 하이드레이션 완료 여부 추가

  setHasHydrated: (state: boolean) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // ... 기존 상태
      hasHydrated: false,

      setHasHydrated: (state) => set({ hasHydrated: state }),
    }),
    {
      name: 'auth-storage',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        user: state.user,
        accessToken: state.accessToken,
        isAuthenticated: state.isAuthenticated,
      }),
      onRehydrateStorage: () => (state) => {
        // ✅ localStorage 복원 완료 시 hasHydrated를 true로 설정
        state?.setHasHydrated(true);
      },
    }
  )
);

// lib/hooks/useAuth.ts
export function useAuth() {
  const { user, isAuthenticated, isLoading, hasHydrated } = useAuthStore();

  return {
    user,
    isAuthenticated,
    isLoading: isLoading || !hasHydrated,  // ✅ 하이드레이션 완료 전까지 로딩 상태
    // ...
  };
}
```

---

### 문제 2: API currentUserId 파라미터 누락으로 500 에러

**현상**:
- ScrapService 호출 시 `Request failed with status code 500`
- PointService는 정상 작동

### 원인
백엔드 API가 `currentUserId` 쿼리 파라미터를 필수로 요구

**백엔드 API 명세** (DOCKER_TEST_GUIDE.md 참고):
```
GET  /api/v1/scrap-folders/me?currentUserId={userId}
POST /api/v1/scrap-folders?currentUserId={userId}
PUT  /api/v1/scrap-folders/{folderId}?currentUserId={userId}
GET  /api/v1/posts/likes/me?currentUserId={userId}
GET  /api/v1/posts/scraps/me?currentUserId={userId}
```

**문제가 있던 코드**:
```typescript
// lib/services/scrapService.ts
getMyFolders: async (): Promise<ScrapFolder[]> => {
  const response = await apiClient.get('/scrap-folders/me');  // ❌ currentUserId 없음
  return response.data.data;
}
```

### 해결 2: 모든 서비스 메서드에 currentUserId 추가

```typescript
// lib/services/scrapService.ts
export const scrapService = {
  getMyFolders: async (currentUserId: number): Promise<ScrapFolder[]> => {
    const response = await apiClient.get('/scrap-folders/me', {
      params: { currentUserId },  // ✅ 쿼리 파라미터 추가
    });
    // ... 응답 처리
  },

  createFolder: async (data: ScrapFolderCreateRequest, currentUserId: number) => {
    const response = await apiClient.post('/scrap-folders', data, {
      params: { currentUserId },  // ✅ POST 요청도 쿼리 파라미터 필요
    });
    // ...
  },

  // 모든 메서드에 동일하게 적용
};

// lib/services/postService.ts
getLikedPosts: async (userId: number, page = 0, size = 20): Promise<Post[]> => {
  const response = await apiClient.get(
    `/posts/likes/me?currentUserId=${userId}&page=${page}&size=${size}`
  );
  // ...
},

getScrappedPosts: async (userId: number, page = 0, size = 20): Promise<Post[]> => {
  const response = await apiClient.get(
    `/posts/scraps/me?currentUserId=${userId}&page=${page}&size=${size}`
  );
  // ...
},
```

**페이지에서 사용**:
```typescript
// app/(main)/scraps/page.tsx
const loadFolders = async () => {
  if (!user) return;

  const foldersData = await scrapService.getMyFolders(user.id);  // ✅ user.id 전달
  // ...
};

// app/(main)/profile/page.tsx
const loadScrappedPosts = async () => {
  if (!user) return;

  const posts = await postService.getScrappedPosts(user.id);  // ✅ user.id 전달
  // ...
};
```

---

### 문제 3: API 응답 구조 불일치로 `.map is not a function` 에러

**현상**:
- Profile 페이지 스크랩 탭: `scrappedPosts.map is not a function`
- Scraps 페이지: `scraps.map is not a function`

### 원인
API 응답이 배열이 아닌 다른 형태로 반환될 수 있음

**가능한 응답 형태**:
```typescript
// 형태 1: 래핑된 응답
{ success: true, data: [...] }

// 형태 2: 직접 배열
[...]

// 형태 3: 빈 객체 또는 null
{}
null
```

### 해결 3: 방어적 배열 처리

```typescript
// lib/services/postService.ts
getLikedPosts: async (userId: number, page = 0, size = 20): Promise<Post[]> => {
  const response = await apiClient.get(
    `/posts/likes/me?currentUserId=${userId}&page=${page}&size=${size}`
  );

  // ✅ 응답이 { success, data } 형태인지 확인
  if (response.data.success && response.data.data) {
    return Array.isArray(response.data.data) ? response.data.data : [];
  }

  // ✅ 배열로 직접 반환되는 경우
  return Array.isArray(response.data) ? response.data : [];
},

// lib/services/scrapService.ts
getScrapsInFolder: async (folderId: number, currentUserId: number): Promise<PostScrap[]> => {
  const response = await apiClient.get(`/posts/scrap-folders/${folderId}/scraps`, {
    params: { currentUserId },
  });

  // ✅ 동일한 방어적 처리
  if (response.data.success && response.data.data) {
    return Array.isArray(response.data.data) ? response.data.data : [];
  }

  return Array.isArray(response.data) ? response.data : [];
},

getMyFolders: async (currentUserId: number): Promise<ScrapFolder[]> => {
  const response = await apiClient.get('/scrap-folders/me', {
    params: { currentUserId },
  });

  // ✅ 폴더 목록도 동일하게 처리
  if (response.data.success && response.data.data) {
    return Array.isArray(response.data.data) ? response.data.data : [];
  }

  return Array.isArray(response.data) ? response.data : [];
},
```

---

### 문제 4: Point 타입 불일치

**현상**:
- Ranking 페이지에서 레벨 표시 오류
- `ranking.level`이 undefined

### 원인
백엔드 API 응답 구조와 프론트엔드 타입 정의 불일치

**백엔드 실제 응답**:
```json
{
  "totalPoints": 150,
  "currentLevel": "LEVEL_2",        // ← 이 필드 사용
  "levelDisplayName": "일반",       // ← 표시용 이름
  "levelNumber": 2                   // ← 숫자로도 제공
}
```

**잘못된 타입 정의**:
```typescript
// lib/types/point.ts (이전)
export interface PointInfo {
  totalPoints: number;
  level: PointLevel;  // ❌ "GOLD", "SILVER" 같은 enum 가정
  // ...
}
```

### 해결 4: 백엔드 응답 구조에 맞게 타입 수정

```typescript
// lib/types/point.ts
export interface PointInfo {
  id: number;
  userId: number;
  totalPoints: number;
  availablePoints: number;
  currentLevel: string;        // ✅ "LEVEL_1", "LEVEL_2", ...
  levelDisplayName: string;    // ✅ "초보", "일반", "고급", ...
  levelNumber: number;         // ✅ 1, 2, 3, 4, 5
  pointsToNextLevel: number;
  // ...
}

export interface PointRanking {
  rank: number;
  user: {
    id: number;
    username: string;
    nickname: string;
  };
  totalPoints: number;
  currentLevel: string;        // ✅ "LEVEL_1", "LEVEL_2", ...
  levelDisplayName: string;    // ✅ "초보", "일반", ...
}
```

**Ranking 페이지 수정**:
```typescript
// app/(main)/ranking/page.tsx
const getLevelColor = (levelNumber: number) => {
  if (levelNumber >= 5) return 'text-blue-400';    // 마스터
  if (levelNumber >= 4) return 'text-purple-400';  // 전문가
  if (levelNumber >= 3) return 'text-yellow-500';  // 고급
  if (levelNumber >= 2) return 'text-gray-400';    // 일반
  return 'text-orange-600';                         // 초보
};

// 레벨 가이드
const levels = [
  { level: 1, name: '초보', points: '0~99', color: 'text-orange-600' },
  { level: 2, name: '일반', points: '100~499', color: 'text-gray-400' },
  { level: 3, name: '고급', points: '500~1,999', color: 'text-yellow-500' },
  { level: 4, name: '전문가', points: '2,000~4,999', color: 'text-purple-400' },
  { level: 5, name: '마스터', points: '5,000+', color: 'text-blue-400' },
];

// 랭킹 렌더링
{rankings.map((ranking) => (
  <div>
    <span className={getLevelColor(parseInt(ranking.currentLevel.replace('LEVEL_', '')))}>
      {ranking.levelDisplayName}
    </span>
  </div>
))}
```

---

### 학습 포인트

#### 1. Zustand Persist 하이드레이션
```typescript
// 하이드레이션 완료 추적 패턴
persist(
  (set, get) => ({
    hasHydrated: false,
    setHasHydrated: (state) => set({ hasHydrated: state }),
  }),
  {
    onRehydrateStorage: () => (state) => {
      state?.setHasHydrated(true);  // localStorage 복원 완료 시 호출
    },
  }
)

// 사용 시 하이드레이션 대기
const { isLoading, hasHydrated } = useAuthStore();
const actuallyLoading = isLoading || !hasHydrated;
```

#### 2. 백엔드 API 파라미터 패턴
```typescript
// GET 요청 - params 옵션 사용
apiClient.get('/endpoint', {
  params: { currentUserId: 1, page: 0 }
});
// → /endpoint?currentUserId=1&page=0

// POST/PUT 요청 - 세 번째 인자로 params
apiClient.post('/endpoint', data, {
  params: { currentUserId: 1 }
});
// → /endpoint?currentUserId=1 + body: data

// DELETE 요청 - 두 번째 인자로 params
apiClient.delete('/endpoint', {
  params: { currentUserId: 1 }
});
```

#### 3. 방어적 배열 처리 패턴
```typescript
// API 응답이 다양한 형태일 수 있으므로 항상 검증
const getData = async (): Promise<Item[]> => {
  const response = await apiClient.get('/endpoint');

  // 1. 래핑된 응답 확인
  if (response.data.success && response.data.data) {
    return Array.isArray(response.data.data) ? response.data.data : [];
  }

  // 2. 직접 배열 확인
  return Array.isArray(response.data) ? response.data : [];
};
```

#### 4. Phase 5 구현 체크리스트
- [ ] 타입 정의: User, Point, Scrap 관련 인터페이스
- [ ] API 서비스: currentUserId 파라미터 추가
- [ ] 하이드레이션: Zustand persist onRehydrateStorage 설정
- [ ] 방어적 처리: 배열 응답 검증
- [ ] 백엔드 응답 구조 확인: DOCKER_TEST_GUIDE.md 참조

#### 5. 디버깅 체크 순서
```
1. 콘솔 에러 확인 → axios 에러인지, 렌더링 에러인지
2. Network 탭 → API 요청/응답 확인
3. 백엔드 문서 → 필수 파라미터, 응답 구조 확인
4. 타입 정의 → 실제 응답과 일치하는지 검증
5. 방어적 처리 → null/undefined/빈 배열 대응
```

---

## 9. Phase 6 레이아웃 구현 - 배열 메서드 에러

### 문제
- 메인 화면(홈페이지)과 Sidebar에서 `popular.slice is not a function` 에러 발생
- 인기 게시글/트렌딩 게시글 로딩 실패

### 원인
`getPopularPosts`와 `getTrendingPosts` API 메서드가 배열이 아닌 값을 반환

**문제가 있던 코드**:
```typescript
// lib/services/postService.ts
getPopularPosts: async (limit = 10): Promise<Post[]> => {
  const response = await apiClient.get<ApiResponse<Post[]>>(`/posts/popular?limit=${limit}`);
  return response.data.data;  // ❌ 항상 배열이라고 가정
},

// components/layout/Sidebar.tsx
const [popular, trending] = await Promise.all([
  postService.getPopularPosts(5),
  postService.getTrendingPosts(7),
]);

setPopularPosts(popular.slice(0, 5));  // ❌ popular가 배열이 아니면 에러
```

### 해결
Phase 5에서 사용한 방어적 배열 처리 패턴을 적용

```typescript
// lib/services/postService.ts
getPopularPosts: async (limit = 10): Promise<Post[]> => {
  const response = await apiClient.get(`/posts/popular?limit=${limit}`);

  // ✅ 응답이 { success, data } 형태인지 확인
  if (response.data.success && response.data.data) {
    return Array.isArray(response.data.data) ? response.data.data : [];
  }

  // ✅ 배열로 직접 반환되는 경우
  return Array.isArray(response.data) ? response.data : [];
},

getTrendingPosts: async (days = 7): Promise<Post[]> => {
  const response = await apiClient.get(`/posts/trending?days=${days}`);

  // ✅ 동일한 패턴 적용
  if (response.data.success && response.data.data) {
    return Array.isArray(response.data.data) ? response.data.data : [];
  }

  return Array.isArray(response.data) ? response.data : [];
},
```

**컴포넌트에서 이중 방어**:
```typescript
// components/layout/Sidebar.tsx
const [popular, trending] = await Promise.all([
  postService.getPopularPosts(5),
  postService.getTrendingPosts(7),
]);

// ✅ 배열인지 확인 후 slice
setPopularPosts(Array.isArray(popular) ? popular.slice(0, 5) : []);
setTrendingPosts(Array.isArray(trending) ? trending.slice(0, 5) : []);

// app/page.tsx (홈페이지)
setPopularPosts(Array.isArray(popular) ? popular : []);
setTrendingPosts(Array.isArray(trending) ? trending : []);
```

### 학습 포인트

#### 1. 일관된 방어적 프로그래밍
```typescript
// 모든 배열 반환 API 메서드에 동일한 패턴 적용
const getData = async (): Promise<Item[]> => {
  const response = await apiClient.get('/endpoint');

  // 1. 래핑된 응답 확인
  if (response.data.success && response.data.data) {
    return Array.isArray(response.data.data) ? response.data.data : [];
  }

  // 2. 직접 배열 확인
  return Array.isArray(response.data) ? response.data : [];
};
```

#### 2. 이중 방어 전략
서비스 레이어와 컴포넌트 레이어 모두에서 검증:
```typescript
// 서비스: API 응답을 배열로 보장
const posts = await postService.getPosts();  // 항상 배열 반환

// 컴포넌트: 추가 방어
setPosts(Array.isArray(posts) ? posts : []);
```

#### 3. TypeScript 타입과 런타임 검증의 차이
```typescript
// TypeScript는 타입만 체크, 런타임 값은 검증 안 함
async function getData(): Promise<Post[]> {
  return response.data;  // TypeScript: OK, 런타임: 배열이 아닐 수 있음
}

// 런타임 검증 필수
return Array.isArray(response.data) ? response.data : [];
```

#### 4. 적용 대상 API 메서드
Phase 5-6에서 방어적 처리를 적용한 메서드들:
- `pointService.getRanking()` - 랭킹 목록
- `postService.getLikedPosts()` - 좋아요한 게시글
- `postService.getScrappedPosts()` - 스크랩한 게시글
- `scrapService.getMyFolders()` - 스크랩 폴더 목록
- `scrapService.getScrapsInFolder()` - 폴더 내 스크랩
- `postService.getPopularPosts()` - 인기 게시글
- `postService.getTrendingPosts()` - 트렌딩 게시글

---

## 참고 파일
- `lib/services/authService.ts` - 인증 API 서비스
- `lib/store/authStore.ts` - Zustand 인증 상태 관리
- `lib/hooks/useAuth.ts` - 인증 훅
- `app/(main)/posts/create/page.tsx` - 게시글 작성 페이지
- `app/(main)/posts/[id]/page.tsx` - 게시글 상세 페이지
- `app/(main)/profile/page.tsx` - 프로필 페이지 (Phase 5)
- `app/(main)/ranking/page.tsx` - 랭킹 페이지 (Phase 5)
- `app/(main)/scraps/page.tsx` - 스크랩 폴더 페이지 (Phase 5)
- `app/page.tsx` - 홈페이지 (Phase 6)
- `lib/services/postService.ts` - 게시글 API 서비스
- `lib/services/scrapService.ts` - 스크랩 API 서비스 (Phase 5)
- `lib/services/pointService.ts` - 포인트 API 서비스 (Phase 5)
- `lib/services/attachmentService.ts` - 첨부파일 API 서비스
- `lib/types/post.ts` - 게시글 타입 정의
- `lib/types/user.ts` - 사용자 타입 정의 (Phase 5)
- `lib/types/point.ts` - 포인트 타입 정의 (Phase 5)
- `lib/types/scrap.ts` - 스크랩 타입 정의 (Phase 5)
- `lib/types/attachment.ts` - 첨부파일 타입 정의
- `components/features/comment/CommentItem.tsx` - 댓글 아이템 컴포넌트
- `components/features/comment/CommentList.tsx` - 댓글 목록 컴포넌트
- `components/layout/Header.tsx` - 헤더 컴포넌트 (Phase 6)
- `components/layout/Footer.tsx` - 푸터 컴포넌트 (Phase 6)
- `components/layout/Sidebar.tsx` - 사이드바 컴포넌트 (Phase 6)

---

## 10. Phase 8 알림 시스템 - 백엔드 API 미구현 에러

### 문제
**날짜**: 2025-12-27

Phase 8 알림 시스템 구현 후, 페이지 로드 시 콘솔에 500 에러 발생:

```
AxiosError: Request failed with status code 500
at lib/services/notificationService.ts (20:22)
```

알림 API를 호출하려 하지만 백엔드에 아직 구현되지 않아 에러 발생

### 원인
Phase 8에서 프론트엔드 알림 시스템을 먼저 구현했지만, 백엔드 API가 아직 준비되지 않음:

```typescript
// lib/services/notificationService.ts
getUnreadCount: async (): Promise<number> => {
  // 백엔드에 /notifications/me/unread-count 엔드포인트가 없음
  const response = await apiClient.get<ApiResponse<{ count: number }>>(
    '/notifications/me/unread-count'
  );
  return response.data.data.count;
}
```

**에러 발생 위치**:
- `NotificationBell.tsx`: 컴포넌트 마운트 시 `loadUnreadCount()` 호출
- 30초마다 자동으로 미읽은 알림 수를 업데이트하려고 시도
- 백엔드 API가 없어 500 에러 발생

### 해결

백엔드 API가 준비되지 않은 경우를 대비해 **Graceful Degradation** 적용:

#### 1. NotificationBell.tsx 에러 핸들링

```typescript
const loadUnreadCount = async () => {
  try {
    const count = await notificationService.getUnreadCount();
    setUnreadCount(count);
  } catch (err) {
    // 백엔드 API가 아직 구현되지 않은 경우 조용히 실패
    console.warn('Notification API not available yet:', err);
    setUnreadCount(0);
  }
};
```

**변경 사항**:
- `console.error` → `console.warn`: 에러 레벨을 경고로 낮춤
- 에러 발생 시 `unreadCount`를 0으로 설정
- UI는 정상적으로 표시되지만 알림 Badge는 나타나지 않음

#### 2. NotificationList.tsx 에러 핸들링

```typescript
const loadNotifications = async () => {
  try {
    setLoading(true);
    const response = await notificationService.getNotifications(0, 10);
    setNotifications(Array.isArray(response.content) ? response.content : []);
  } catch (err) {
    // 백엔드 API가 아직 구현되지 않은 경우 조용히 실패
    console.warn('Notification API not available yet:', err);
    setNotifications([]);
  } finally {
    setLoading(false);
  }
};
```

**변경 사항**:
- 에러 발생 시 빈 배열로 설정
- "알림이 없습니다" 메시지 표시
- 사용자는 API 에러를 인지하지 못하고 단순히 알림이 없는 것으로 보임

#### 3. 기타 에러 핸들링

`handleMarkAsRead`, `handleMarkAllAsRead`, `handleDelete` 메서드도 동일하게 `console.warn`으로 변경

### 학습 포인트

#### 1. **Progressive Enhancement vs Graceful Degradation**

- **Progressive Enhancement**: 기본 기능을 먼저 구현하고 점진적으로 향상
- **Graceful Degradation**: 모든 기능을 구현하되, 실패 시 우아하게 저하

알림 시스템은 Graceful Degradation 접근:
- 알림 기능이 전부 구현되어 있음
- 백엔드 API가 없어도 앱이 정상 작동
- API가 준비되면 자동으로 동작

#### 2. **에러 레벨 구분**

```typescript
console.error()  // 🔴 반드시 수정해야 하는 에러
console.warn()   // 🟡 알아야 하지만 앱은 계속 작동
console.log()    // ⚪ 일반 로그
console.info()   // 🔵 정보성 메시지
```

백엔드 미구현은 "알아야 하지만 앱은 계속 작동"하므로 `warn` 적합

#### 3. **Optional Features 패턴**

알림 시스템처럼 선택적 기능은 다음 패턴으로 구현:

```typescript
try {
  const data = await optionalFeatureAPI();
  setData(data);
} catch (err) {
  console.warn('Optional feature not available:', err);
  setData(defaultValue); // 기본값 사용
}
```

#### 4. **사용자 경험 우선**

에러가 발생해도:
- UI가 깨지지 않음
- 에러 메시지가 사용자에게 노출되지 않음
- 알림 벨 아이콘은 표시되지만 Badge만 없음
- 클릭하면 "알림이 없습니다" 표시

### 향후 계획

백엔드 알림 API가 구현되면:
1. 에러 핸들링 코드는 그대로 유지 (네트워크 에러 대비)
2. API가 정상 작동하면 자동으로 알림 기능 활성화
3. 추가 코드 수정 불필요

### 참고 파일
- `components/features/notification/NotificationBell.tsx` - 알림 벨 컴포넌트
- `components/features/notification/NotificationList.tsx` - 알림 목록 컴포넌트
- `lib/services/notificationService.ts` - 알림 API 서비스

---

## 11. 좋아요/스크랩 기능 - currentUserId 파라미터 누락 에러

### 문제
**날짜**: 2025-12-27

게시글 상세 페이지에서 좋아요 버튼 클릭 시 500 에러 발생:

```
AxiosError: Request failed with status code 500
at lib/services/postService.ts (81:22)
POST http://3.39.36.234:8080/api/v1/posts/{postId}/like
```

### 원인
백엔드 API는 정상적으로 작동하지만, 프론트엔드에서 필수 파라미터 `currentUserId`를 누락하고 호출함

**백엔드 API 요구사항** (DOCKER_TEST_GUIDE.md):
```
POST /api/v1/posts/{postId}/like?currentUserId={userId}
POST /api/v1/posts/{postId}/scrap?currentUserId={userId}
```

**프론트엔드 기존 코드** (잘못된 코드):
```typescript
// lib/services/postService.ts (잘못된 코드)
toggleLike: async (postId: number) => {
  // ❌ currentUserId 파라미터 없음!
  const response = await apiClient.post(`/posts/${postId}/like`);
  return response.data.data;
}

toggleScrap: async (postId: number, folderId?: number) => {
  // ❌ currentUserId 파라미터 없음!
  const response = await apiClient.post(`/posts/${postId}/scrap`, { folderId });
  return response.data.data;
}
```

프론트엔드가 필수 파라미터 없이 호출 → 백엔드가 400 또는 500 에러 반환

### 해결

#### 1. postService.ts 수정 - currentUserId 파라미터 추가

```typescript
// lib/services/postService.ts (수정된 코드)
toggleLike: async (postId: number, currentUserId: number): Promise<{ liked: boolean; likeCount: number }> => {
  // ✅ currentUserId 파라미터 추가
  const response = await apiClient.post<ApiResponse<{ liked: boolean; likeCount: number }>>(
    `/posts/${postId}/like?currentUserId=${currentUserId}`
  );
  return response.data.data;
},

toggleScrap: async (postId: number, currentUserId: number, folderId?: number): Promise<{ scraped: boolean }> => {
  // ✅ currentUserId 파라미터 추가
  const response = await apiClient.post<ApiResponse<{ scraped: boolean }>>(
    `/posts/${postId}/scrap?currentUserId=${currentUserId}`,
    { folderId }
  );
  return response.data.data;
},
```

**변경 사항**:
- 함수 시그니처에 `currentUserId: number` 파라미터 추가
- API 호출 시 쿼리 파라미터로 `currentUserId` 전달
- TypeScript 타입 체크로 누락 방지

#### 2. 게시글 상세 페이지 수정 - user.id 전달

```typescript
// app/(main)/posts/[id]/page.tsx (수정된 코드)
const handleLike = async () => {
  if (!isAuthenticated || !user?.id) {
    alert('로그인이 필요합니다.');
    router.push(`/login?redirect=/posts/${postId}`);
    return;
  }

  try {
    // ✅ user.id를 currentUserId로 전달
    const result = await postService.toggleLike(postId, user.id);
    setLiked(result.liked);
    setLikeCount(result.likeCount);
  } catch (err: any) {
    console.error('Failed to toggle like:', err);
    alert(err.response?.data?.message || '좋아요 처리에 실패했습니다.');
  }
};

const handleScrap = async () => {
  if (!isAuthenticated || !user?.id) {
    alert('로그인이 필요합니다.');
    router.push(`/login?redirect=/posts/${postId}`);
    return;
  }

  try {
    // ✅ user.id를 currentUserId로 전달
    const result = await postService.toggleScrap(postId, user.id);
    setScraped(result.scraped);
    alert(result.scraped ? '스크랩했습니다.' : '스크랩을 취소했습니다.');
  } catch (err: any) {
    console.error('Failed to toggle scrap:', err);
    alert(err.response?.data?.message || '스크랩 처리에 실패했습니다.');
  }
};
```

**변경 사항**:
- `user?.id` null 체크 추가
- `postService.toggleLike(postId, user.id)` - user.id 전달
- `postService.toggleScrap(postId, user.id)` - user.id 전달
- 불필요한 500 에러 핸들링 제거 (이제 정상 작동)

### 학습 포인트

#### 1. **API 문서 확인의 중요성**

백엔드 API 문서를 정확히 확인해야 함:
- 필수 파라미터
- 선택적 파라미터
- 요청 방식 (GET, POST, etc.)
- 응답 형식

```typescript
// DOCKER_TEST_GUIDE.md 확인
POST /api/v1/posts/{postId}/like?currentUserId={userId}
                                  ^^^^^^^^^^^^^^^^^^^^
                                  필수 파라미터!
```

#### 2. **TypeScript 활용**

파라미터 누락을 컴파일 타임에 잡기:

```typescript
// ✅ TypeScript가 에러 감지
toggleLike: async (postId: number, currentUserId: number) => { ... }

// 호출 시
postService.toggleLike(postId);  // ❌ Error: Expected 2 arguments, but got 1
postService.toggleLike(postId, user.id);  // ✅ OK
```

#### 3. **일관성 있는 API 디자인**

다른 API 메서드들도 동일한 패턴 확인:
- `createPost(data, currentUserId)` ✅
- `updatePost(postId, data, currentUserId)` ✅
- `deletePost(postId, currentUserId)` ✅
- `publishPost(postId, currentUserId)` ✅
- `toggleLike(postId, currentUserId)` ✅ (수정 완료)
- `toggleScrap(postId, currentUserId)` ✅ (수정 완료)

모든 mutating 작업에 `currentUserId` 필요

### 향후 계획

1. 다른 API 메서드들도 `currentUserId` 패턴 확인
2. TypeScript strict mode 활성화로 타입 체크 강화
3. API 클라이언트 wrapper 함수로 currentUserId 자동 주입 고려

### 추가 수정 사항

**문제**: API는 정상 작동하지만 UI가 업데이트되지 않음

**원인**: 백엔드 응답 프로퍼티 이름과 프론트엔드 기대값 불일치

```typescript
// 백엔드 응답
{
  isLiked: true,
  totalLikeCount: 1
}

// 프론트엔드 기대값
{
  liked: boolean,
  likeCount: number
}
```

**해결**: postService.ts에서 응답 매핑 추가

```typescript
toggleLike: async (postId: number, currentUserId: number): Promise<{ liked: boolean; likeCount: number }> => {
  const response = await apiClient.post<ApiResponse<{ isLiked: boolean; totalLikeCount: number }>>(
    `/posts/${postId}/like?currentUserId=${currentUserId}`
  );
  // 백엔드 응답을 프론트엔드 형식으로 변환
  return {
    liked: response.data.data.isLiked,
    likeCount: response.data.data.totalLikeCount,
  };
},
```

### 추가 수정 사항 2: 스크랩 API 요청 본문 및 토글 로직 에러

**문제 1**: 스크랩 기능 호출 시 검증 에러 발생 (400 Bad Request)

백엔드 API는 요청 본문에 `postId`와 `folderId`를 모두 포함해야 함:
```json
{
  "postId": 1,
  "folderId": null
}
```

프론트엔드는 이 필드들을 보내지 않아서 에러 발생

**문제 2**: 스크랩 버튼을 두 번 클릭하면 400 에러 발생

백엔드는 토글 API가 아니라 **별도의 추가/삭제 엔드포인트**를 제공:
- **POST** `/api/v1/posts/{postId}/scrap` - 스크랩 추가
- **DELETE** `/api/v1/posts/{postId}/scrap` - 스크랩 삭제

프론트엔드가 항상 POST만 호출해서 이미 스크랩된 게시글을 다시 추가하려고 시도 → 400 에러

**원인**:
1. 요청 본문에 필수 필드 `postId` 누락
2. API를 토글로 잘못 이해하고 구현

**해결**: 컴포넌트의 현재 상태를 신뢰하고 적절한 메서드(POST/DELETE) 호출

```typescript
// lib/services/postService.ts
toggleScrap: async (
  postId: number,
  currentUserId: number,
  currentScrapedState: boolean,  // ✅ 현재 상태를 파라미터로 받음
  folderId?: number
): Promise<{ scraped: boolean }> => {
  if (currentScrapedState) {
    // 이미 스크랩되어 있으면 DELETE로 취소
    await apiClient.delete(`/posts/${postId}/scrap?currentUserId=${currentUserId}`);
    return { scraped: false };
  } else {
    // 스크랩되어 있지 않으면 POST로 추가
    const requestBody = {
      postId,                    // ✅ postId 필수
      folderId: folderId || null, // ✅ folderId null 허용
    };
    await apiClient.post(`/posts/${postId}/scrap?currentUserId=${currentUserId}`, requestBody);
    return { scraped: true };
  }
}

// app/(main)/posts/[id]/page.tsx
const handleScrap = async () => {
  try {
    // ✅ 현재 scraped 상태를 전달
    const result = await postService.toggleScrap(postId, user.id, scraped);
    setScraped(result.scraped);

    // ✅ 성공 후 백엔드와 동기화
    await loadScrapStatus();

    alert(result.scraped ? '스크랩했습니다.' : '스크랩을 취소했습니다.');
  } catch (err: any) {
    console.error('Failed to toggle scrap:', err);
    alert(err.response?.data?.message || '스크랩 처리에 실패했습니다.');

    // ✅ 에러 시에도 상태 동기화
    await loadScrapStatus();
  }
}
```

**학습 포인트**:
- **API 엔드포인트 이해**: Toggle API vs 별도 Add/Remove API 구분
- **상태 신뢰**: API로 상태를 다시 조회하는 대신 컴포넌트의 현재 상태 사용 (race condition 방지)
- **낙관적 업데이트 + 동기화**: 즉시 UI 업데이트 후 백엔드와 재동기화
- **백엔드 API 명세 준수**: 요청 본문 형식을 정확히 따라야 함
- **RESTful 패턴**: POST(생성), DELETE(삭제)를 각각 사용하는 것이 표준
- **에러 복구**: 에러 발생 시에도 상태를 다시 로드하여 UI와 백엔드 동기화

### 추가 수정 사항 3: 스크랩 상태 API 응답 형식 불일치 (최종 해결)

**문제**: 스크랩 상태가 `undefined`로 설정되어 UI에 반영되지 않음

디버깅 로그:
```
getScrapStatus API response: {success: true, data: true, timestamp: '...'}
Scrap status loaded: true
Current scraped state before toggle: undefined  // ← 문제!
```

**원인**: 백엔드와 프론트엔드의 응답 형식 불일치

**백엔드 실제 응답**:
```json
{
  "success": true,
  "data": true,  // ← 불리언 값만 반환
  "timestamp": "2025-12-27T15:15:42.281589015"
}
```

**프론트엔드 기대 형식**:
```json
{
  "success": true,
  "data": {
    "scraped": true  // ← 객체 형태 기대
  }
}
```

**결과적으로 발생한 문제**:
```typescript
const status = await getScrapStatus(postId, user.id);
// status = true (불리언)

setScraped(status.scraped);
// true.scraped = undefined!
```

**해결**: 백엔드 응답을 프론트엔드 형식으로 변환

```typescript
// lib/services/postService.ts
getScrapStatus: async (postId: number, currentUserId?: number): Promise<{ scraped: boolean }> => {
  const params = currentUserId ? { currentUserId } : {};
  const response = await apiClient.get<ApiResponse<boolean>>(`/posts/${postId}/scrap/status`, { params });

  // ✅ 백엔드는 data: true/false (불리언)를 반환
  // ✅ 프론트엔드는 { scraped: boolean } 형태 필요
  const scrapedValue = response.data.data;
  return { scraped: scrapedValue };  // 불리언을 객체로 변환
}
```

**동작 확인**:
```
getScrapStatus API response: {data: true}
→ return { scraped: true }
→ setScraped(status.scraped)  // status.scraped = true ✅
→ UI에 노란색 버튼 표시 ✅
```

**학습 포인트**:
- **응답 형식 검증**: 백엔드 API 실제 응답과 프론트엔드 기대값이 일치하는지 확인
- **타입 변환 레이어**: 서비스 레이어에서 백엔드 형식을 프론트엔드 형식으로 변환
- **디버깅 로깅**: console.log로 실제 API 응답을 확인하여 가정과 실제 불일치 발견
- **TypeScript 한계**: 타입 정의만으로는 런타임 데이터 형식 불일치를 잡을 수 없음
- **Adapter Pattern**: 서로 다른 인터페이스를 연결하는 어댑터 역할

### 참고 파일
- `app/(main)/posts/[id]/page.tsx` - 게시글 상세 페이지
- `lib/services/postService.ts` - 게시글 API 서비스

---

## 12. Phase 8 다크모드 구현 - globals.css 설정 문제

### 문제
**날짜**: 2025-12-27

다크모드 토글 버튼을 클릭하면 `<html>` 태그에 `dark` 클래스가 정상적으로 추가되지만, 화면 색상이 변경되지 않음

**디버깅 로그**:
```
[ThemeToggle] Setting theme to: dark
[themeStore] setTheme called with: dark
[applyTheme] Added dark class
[ThemeProvider] Updated classes: dark  // ✅ dark 클래스는 추가됨
```

### 원인
`globals.css`에서 다크모드 스타일이 `@media (prefers-color-scheme: dark)` (시스템 설정 기반)으로 정의되어 있었음

**문제가 있던 코드**:
```css
/* app/globals.css */
:root {
  --background: #ffffff;
  --foreground: #171717;
}

@media (prefers-color-scheme: dark) {
  :root {
    --background: #0a0a0a;
    --foreground: #ededed;
  }
}
```

**문제점**:
- Tailwind는 `darkMode: 'class'`로 설정되어 `.dark` 클래스 기반 동작
- 하지만 `globals.css`는 시스템 설정(`prefers-color-scheme`)을 따르도록 설정
- 토글 버튼으로 `dark` 클래스를 추가해도 CSS가 반응하지 않음

### 해결

`globals.css`를 `.dark` 클래스 기반으로 변경:

```css
/* app/globals.css (수정 후) */
:root {
  --background: #ffffff;
  --foreground: #171717;
}

.dark {
  --background: #0a0a0a;
  --foreground: #ededed;
}

body {
  color: var(--foreground);
  background: var(--background);
  font-family: Arial, Helvetica, sans-serif;
}
```

**변경 사항**:
- `@media (prefers-color-scheme: dark)` → `.dark` 클래스 선택자
- 이제 `<html class="dark">`가 추가되면 CSS 변수가 다크모드 값으로 변경됨

### 학습 포인트

#### 1. **Tailwind 다크모드 설정 일관성**

`tailwind.config.ts`와 `globals.css`의 설정이 일치해야 함:

```typescript
// tailwind.config.ts
export default {
  darkMode: 'class',  // ✅ 클래스 기반 다크모드
  // ...
}
```

```css
/* globals.css */
.dark {  /* ✅ 클래스 선택자 사용 */
  --background: #0a0a0a;
  --foreground: #ededed;
}
```

#### 2. **다크모드 구현 방식 비교**

| 방식 | 장점 | 단점 | 적합한 경우 |
|------|------|------|------------|
| `darkMode: 'media'` | 시스템 설정 자동 반영 | 사용자가 직접 제어 불가 | 시스템 설정만 따르는 경우 |
| `darkMode: 'class'` | 사용자가 직접 제어 가능 | 토글 기능 구현 필요 | 토글 버튼으로 제어하는 경우 |

#### 3. **전체 다크모드 구현 체크리스트**

1. **Tailwind 설정** (`tailwind.config.ts`):
   ```typescript
   darkMode: 'class'
   ```

2. **Global CSS** (`globals.css`):
   ```css
   .dark {
     --background: #0a0a0a;
     --foreground: #ededed;
   }
   ```

3. **상태 관리** (`lib/store/themeStore.ts`):
   - Zustand persist로 localStorage 저장
   - `applyTheme()` 함수로 `<html>` 클래스 조작

4. **Provider 컴포넌트** (`components/providers/ThemeProvider.tsx`):
   - `initializeTheme()` 초기화
   - `resolvedTheme` 변경 시 자동 적용

5. **토글 버튼** (`components/ui/ThemeToggle.tsx`):
   - 사용자 인터랙션 처리
   - 아이콘 변경 (해/달)

6. **컴포넌트 스타일링**:
   - 모든 주요 컴포넌트에 `dark:` 접두사 클래스 추가
   - 예: `bg-white dark:bg-gray-800`

#### 4. **디버깅 팁**

다크모드가 작동하지 않을 때 확인 사항:

```bash
# 1. HTML 클래스 확인
개발자 도구 > Elements > <html> 태그에 class="dark" 있는지 확인

# 2. CSS 변수 확인
개발자 도구 > Elements > Styles 탭 > --background 값 변경되는지 확인

# 3. Tailwind 컴파일 확인
개발 서버 재시작 후 Ctrl+Shift+R (강력 새로고침)
```

#### 5. **CSS 우선순위 이해**

```css
/* 낮은 우선순위 */
@media (prefers-color-scheme: dark) { ... }

/* 높은 우선순위 */
.dark { ... }

/* 가장 높은 우선순위 */
.dark .specific-class { ... }
```

`.dark` 클래스 선택자가 미디어 쿼리보다 명시도가 높아 우선 적용됨

### 적용된 컴포넌트 목록

다크모드 클래스가 적용된 컴포넌트들:

1. **레이아웃**:
   - `app/layout.tsx` - ThemeProvider 래핑
   - `app/page.tsx` - 메인 페이지
   - `components/layout/Header.tsx` - 헤더
   - `components/layout/Footer.tsx` - 푸터
   - `components/layout/Sidebar.tsx` - 사이드바

2. **페이지**:
   - `app/(main)/posts/page.tsx` - 게시글 목록
   - `app/(main)/posts/[id]/page.tsx` - 게시글 상세 (TODO)
   - `app/(main)/scraps/page.tsx` - 스크랩 폴더
   - 기타 페이지들 (TODO)

3. **컴포넌트**:
   - `components/features/post/PostCard.tsx` - 게시글 카드
   - `components/ui/ThemeToggle.tsx` - 테마 토글 버튼
   - `components/providers/ThemeProvider.tsx` - 테마 프로바이더

### 향후 작업

나머지 페이지와 컴포넌트에도 다크모드 클래스 적용 필요:
- 게시글 상세 페이지
- 댓글 컴포넌트
- 프로필 페이지
- 랭킹 페이지
- 로그인/회원가입 페이지
- 게시글 작성/수정 페이지

### 참고 파일
- `tailwind.config.ts` - Tailwind 설정
- `app/globals.css` - 전역 CSS
- `lib/store/themeStore.ts` - 테마 상태 관리
- `components/providers/ThemeProvider.tsx` - 테마 프로바이더
- `components/ui/ThemeToggle.tsx` - 테마 토글 버튼

---

## 13. React Hydration 에러 - Input 컴포넌트 ID 불일치

### 문제
**날짜**: 2025-12-28

로그인 페이지에서 React hydration 에러 발생:
```
A tree hydrated but some attributes of the server rendered HTML didn't match the client properties.
```

Input 요소의 `id`와 `htmlFor` 속성이 서버와 클라이언트에서 다른 값을 가짐:
- 서버: `input-ns0fbpgkx`
- 클라이언트: `input-my2caz8d0`

### 원인

Input 컴포넌트에서 `Math.random()`으로 ID를 동적 생성:

```typescript
// components/ui/Input.tsx (문제가 있던 코드)
const inputId = id || `input-${Math.random().toString(36).substr(2, 9)}`;
```

**문제점**:
- 서버 렌더링 시 생성된 ID와 클라이언트 hydration 시 생성된 ID가 다름
- `Math.random()`은 매번 다른 값을 반환하므로 불일치 발생
- React는 서버와 클라이언트의 HTML이 정확히 일치해야 함

### 해결

React 18의 `useId` hook 사용:

```typescript
// components/ui/Input.tsx (수정 후)
import { InputHTMLAttributes, forwardRef, useId } from 'react';

const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, helperText, fullWidth = true, className = '', id, disabled, ...props }, ref) => {
    const autoId = useId();  // ✅ React가 서버/클라이언트 동일한 ID 생성
    const inputId = id || autoId;
    // ...
  }
);
```

**변경 사항**:
- `Math.random()` → `useId()` hook
- `useId()`는 서버와 클라이언트에서 일관된 ID를 생성
- Hydration 에러 해결

### 학습 포인트

#### 1. React Hydration 이해
- 서버에서 생성한 HTML과 클라이언트에서 렌더링한 HTML이 정확히 일치해야 함
- 불일치 시 React는 경고를 표시하고 클라이언트 HTML로 교체

#### 2. useId Hook (React 18+)
```typescript
const id = useId();  // 서버/클라이언트 일관된 ID 생성
```

**사용 시기**:
- 접근성 속성 연결 (`htmlFor`, `aria-describedby`)
- 서버 사이드 렌더링 환경
- 고유 ID가 필요한 경우

**사용하지 말아야 할 경우**:
- 리스트 아이템의 `key` prop
- CSS 선택자 (불안정한 ID)

#### 3. Hydration 에러 원인
- `Math.random()`, `Date.now()` 같은 비결정적 값
- 브라우저 전용 API (typeof window !== 'undefined')
- 타임존/로케일 차이
- 외부 데이터 변경

### 참고 파일
- `components/ui/Input.tsx` - Input 컴포넌트

---

## 14. Input Autocomplete 경고

### 문제
**날짜**: 2025-12-28

브라우저 콘솔에 DOM 경고 발생:
```
[DOM] Input elements should have autocomplete attributes (suggested: "current-password")
```

### 원인

로그인 페이지의 이메일/비밀번호 Input에 `autocomplete` 속성 누락

**문제점**:
- 브라우저가 자동완성 기능을 제공하지 못함
- 접근성 및 사용자 경험 저하
- 비밀번호 관리자가 제대로 작동하지 않을 수 있음

### 해결

Input 요소에 적절한 `autoComplete` 속성 추가:

```typescript
// app/(auth)/login/page.tsx (수정 후)
<Input
  label="이메일"
  type="email"
  value={formData.email}
  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
  autoComplete="email"  // ✅ 이메일 자동완성
  required
/>

<Input
  label="비밀번호"
  type="password"
  value={formData.password}
  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
  autoComplete="current-password"  // ✅ 현재 비밀번호 자동완성
  required
/>
```

### 학습 포인트

#### 1. 주요 autocomplete 값

| 값 | 용도 |
|---|---|
| `email` | 이메일 주소 |
| `current-password` | 로그인 시 현재 비밀번호 |
| `new-password` | 회원가입/비밀번호 변경 시 새 비밀번호 |
| `username` | 사용자 이름 |
| `name` | 전체 이름 |
| `tel` | 전화번호 |
| `off` | 자동완성 비활성화 |

#### 2. 보안 및 UX 향상
- 비밀번호 관리자 통합
- 자동완성으로 입력 속도 향상
- 모바일에서 적절한 키보드 표시

#### 3. Input 컴포넌트 재사용성
```typescript
// Input 컴포넌트는 {...props}로 모든 HTML 속성 지원
<Input autoComplete="email" {...otherProps} />
```

### 참고 파일
- `app/(auth)/login/page.tsx` - 로그인 페이지

---
