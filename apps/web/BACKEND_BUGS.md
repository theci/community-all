# 백엔드 API 버그 리스트

프론트엔드 개발 중 발견된 백엔드 API 버그들을 기록합니다.

---

## 🔴 CRITICAL: 스크랩 API - 게시글 정보 누락

**날짜**: 2025-12-27
**심각도**: CRITICAL
**API**: `GET /api/v1/posts/scrap-folders/{folderId}/scraps?currentUserId={userId}`

### 문제

스크랩 목록 조회 시 필수 정보가 누락되어 반환됩니다:

**현재 응답**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 5,
        "createdAt": "2025-12-27 15:24:59",
        "post": null,        // ❌ null
        "scrapFolder": null  // ❌ null
        // ❌ postId 필드 자체가 없음!
        // ❌ userId 필드 자체가 없음!
        // ❌ folderId 필드 자체가 없음!
      }
    ],
    "pageInfo": {...}
  }
}
```

### 기대하는 응답

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 5,
        "postId": 13,        // ✅ 필수: 게시글 ID
        "userId": 1,         // ✅ 필수: 사용자 ID
        "folderId": 2,       // ✅ 필수: 폴더 ID
        "createdAt": "2025-12-27 15:24:59",
        "post": {            // ✅ 권장: 게시글 전체 정보 (JOIN)
          "id": 13,
          "title": "게시글 제목",
          "content": "...",
          "author": {...},
          "viewCount": 10,
          "likeCount": 5,
          "commentCount": 3,
          ...
        },
        "scrapFolder": {     // ✅ 선택: 폴더 정보
          "id": 2,
          "name": "기본 폴더",
          ...
        }
      }
    ],
    "pageInfo": {...}
  }
}
```

### 영향

- 스크랩 목록에 제목 표시 불가 ("제목 없음"으로 표시)
- 게시글로 이동 불가 (postId 없음)
- 사용자 경험 심각하게 저하

### 임시 해결책 (프론트엔드)

1. 타입을 optional로 수정하여 크래시 방지
2. postId 없을 시 경고 메시지 표시
3. post 정보 없을 시 기본 UI 표시

### 백엔드 수정 필요 사항

1. **필수**: `postId`, `userId`, `folderId` 필드 반환
2. **권장**: `post` 객체를 JOIN하여 포함 (N+1 쿼리 방지)
3. **선택**: `scrapFolder` 객체 포함

### 관련 파일

- `frontend/lib/types/engagement.ts` - PostScrap 타입
- `frontend/lib/services/scrapService.ts` - getScrapsInFolder
- `frontend/app/(main)/scraps/page.tsx` - 스크랩 페이지

### 백엔드 확인 필요

```java
// PostScrapController 또는 ScrapService
// GET /api/v1/posts/scrap-folders/{folderId}/scraps

// 1. DTO에 postId, userId, folderId 포함 확인
// 2. @EntityGraph 또는 fetch join으로 post 정보 로딩
// 3. 응답 매핑 시 모든 필드 포함 확인
```

### 테스트 방법

```bash
# 스크랩 추가 후 조회
curl -X POST "http://3.39.36.234:8080/api/v1/posts/13/scrap?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"postId": 13, "folderId": null}'

# 스크랩 목록 조회
curl "http://3.39.36.234:8080/api/v1/posts/scrap-folders/2/scraps?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"

# 응답에 postId, post 객체가 포함되어야 함
```

---

## 참고: 다른 API와 비교

### 좋아요 API (정상 동작)

```
POST /api/v1/posts/{postId}/like?currentUserId={userId}
```
→ `isLiked`, `totalLikeCount` 정상 반환 ✅

### 게시글 목록 API (정상 동작)

```
GET /api/v1/posts?page=0&size=20
```
→ 게시글 전체 정보 포함 ✅

### 스크랩 상태 API (정상 동작)

```
GET /api/v1/posts/{postId}/scrap/status?currentUserId={userId}
```
→ boolean 값 정상 반환 (프론트엔드에서 변환 필요했지만 데이터는 있음) ✅

### 스크랩 목록 API (버그)

```
GET /api/v1/posts/scrap-folders/{folderId}/scraps?currentUserId={userId}
```
→ postId 누락, post 정보 null ❌

---

## 우선순위

이 버그는 **CRITICAL** 우선순위입니다:
- 스크랩 기능의 핵심 기능 불가
- 사용자가 스크랩한 게시글을 볼 수 없음
- 데이터베이스에는 저장되어 있으나 조회 불가

**즉시 수정 필요합니다.**
