# 🐳 Community Platform API - Docker 테스트 가이드

## 📚 프로젝트 개요

### 시스템 아키텍처
**Domain-Driven Design (DDD) 기반 커뮤니티 플랫폼 API**

```
├── 📦 Domain Layer (도메인 계층)
│   ├── User (사용자): 회원가입, 인증, 프로필 관리
│   ├── Content (콘텐츠): 게시글, 댓글, 카테고리, 태그
│   ├── Engagement (참여): 좋아요, 스크랩, 스크랩 폴더
│   ├── Notification (알림): 시스템 알림
│   ├── Authorization (권한): 역할 기반 접근 제어 (RBAC)
│   ├── Reward (보상): 포인트 시스템
│   └── Moderation (중재): 신고 시스템
│
├── 🔧 Application Layer (응용 계층)
│   └── Service 클래스들 (비즈니스 로직 처리)
│
├── 🌐 Presentation Layer (표현 계층)
│   └── REST API Controllers
│
└── 🗄️ Infrastructure Layer (인프라 계층)
    ├── JPA Repository (데이터 영속성)
    ├── QueryDSL (동적 쿼리)
    └── Security (Spring Security + JWT)
```

### 기술 스택
- **Framework**: Spring Boot 3.2.0, Spring Data JPA, Spring Security
- **Database**: H2 (개발), PostgreSQL/MySQL (운영)
- **Auth**: JWT 토큰 기반 인증
- **Object Mapping**: MapStruct, Lombok
- **Build**: Gradle 8.5, Docker
- **Query**: QueryDSL 5.0.0

### 주요 기능
✅ **구현 완료**
- **사용자 관리**: 회원가입, 로그인, JWT 인증
- **게시글 관리**: CRUD, 검색, 발행, 인기글, 트렌딩
- **댓글 관리**: CRUD, 대댓글, 계층형 구조
- **좋아요 관리**: 게시글 좋아요/취소, 좋아요 목록 조회
- **스크랩 관리**: 게시글 스크랩, 스크랩 폴더 관리
- **포인트 시스템**: 포인트 적립/사용, 레벨 시스템, 랭킹 ✨ NEW!
- **역할 기반 접근 제어**: 5단계 역할, 29개 세부 권한, 자동 승격 ✨ NEW!
- **Redis 연결**: AWS ElastiCache 연동 완료
- 카테고리 관리 (계층형 구조)
- 태그 시스템
- Spring Security 설정

✅ **구현 완료**
- 신고 관리 시스템 ✨ NEW!
- 사용자 제재 & 패널티 시스템 ✨ NEW! (Phase 1)
- 알림 시스템 ✨ NEW! (Phase 3)

🚧 **진행 중** (Phase 6)
- 콘텐츠 필터링
- 공지사항 & 큐레이션

🔴 **미구현**
- 실시간 기능 (WebSocket/SSE)

---

## ⚠️ 빌드 방법 참고!!
docker-compose down && docker build --no-cache -t community-platform

Bash(./gradlew clean build -x test) timeout: 3m 0s
  ⎿  Error: Exit code 127
     /bin/bash: line 1: ./gradlew: No such file or directory

● Bash(./gradlew clean build -x test) timeout: 3m 0s
  ⎿  Error: Exit code 127
     /bin/bash: line 1: ./gradlew: No such file or directory

● Bash(ls -la | grep gradle)
  ⎿  drwxr-xr-x.  5 ec2-user ec2-user    56 Dec 21 05:13 .gradle     
     -rw-r--r--.  1 ec2-user ec2-user  3728 Dec 22 13:04 build.gradle

● Bash(docker build --no-cache -t community-platform .) timeout: 5m 0s
  ⎿  Error: Exit code 1
     #0 building with "default" instance using docker driver

     #1 [internal] load build definition from Dockerfile
     #1 transferring dockerfile: 1.54kB done
     #1 DONE 0.0s

     #2 [internal] load metadata for docker.io/library/gradle:8.5-jdk17-alpine
     #2 DONE 0.6s

docker build --no-cache . 2>&1 | grep -A 20 "error:"
에러 원인 : 코드 에러를 찾았습니다! UserPointService를 찾을 수 없습니다. 확인하겠습니다.
 타입 에러가 발생했습니다. UserStatus 타입 문제를 수정하겠습니다.
 ADMIN_ADJUST가 없습니다. PointTransactionType을 확인하겠습니다. 
 ADMIN_GRANT와 ADMIN_DEDUCT를 사용해야 합니다. 수정하겠습니다.
    211 -            userPointService.spendPoints(userId, Math.abs(request.getPoints()), request.getReason());                                                                                                                                                                 
    211 +            pointService.spendPoints(userId, Math.abs(request.getPoints()), request.getReason());  

    129 -        List<UserPenalty> penalties = userPenaltyRepository.findByTargetUserId(userId);                                                                                                                                                                               
    129 +        List<UserPenalty> penalties = userPenaltyRepository.findActiveByUserId(userId);    

### ✅ 모든 주요 API 활성화 완료!
모든 핵심 기능의 컨트롤러가 활성화되어 정상적으로 작동합니다.

### ✅ 테스트 가능한 API

#### 🔐 **인증 API** (AuthController, UserService)
- **POST** `/api/v1/users/register` - 회원가입
- **POST** `/api/v1/auth/login` - 로그인
- **POST** `/api/v1/auth/refresh` - 토큰 갱신
- **POST** `/api/v1/auth/logout` - 로그아웃 (인증 필요)

#### 📝 **게시글 API** (PostController, PostService) 
- **GET** `/api/v1/posts` - 게시글 목록 조회
- **GET** `/api/v1/posts/{postId}` - 게시글 상세 조회
- **POST** `/api/v1/posts` - 게시글 작성 (인증 필요)
- **POST** `/api/v1/posts/{postId}/publish` - 게시글 발행 (인증 필요)
- **PUT** `/api/v1/posts/{postId}` - 게시글 수정 (인증 필요)
- **DELETE** `/api/v1/posts/{postId}` - 게시글 삭제 (인증 필요)
- **GET** `/api/v1/posts/search` - 게시글 검색
- **GET** `/api/v1/posts/popular` - 인기 게시글 조회
- **GET** `/api/v1/posts/trending` - 트렌딩 게시글 조회
- **GET** `/api/v1/posts/notices` - 공지사항 목록 조회
- **GET** `/api/v1/posts/category/{categoryId}` - 카테고리별 게시글 조회
- **GET** `/api/v1/posts/author/{authorId}` - 작성자별 게시글 조회
- **GET** `/api/v1/posts/{postId}/similar` - 유사 게시글 조회

#### 📁 **카테고리 API** (CategoryService)
- **GET** `/api/v1/categories/tree` - 카테고리 트리 조회
- **GET** `/api/v1/categories/root` - 최상위 카테고리 목록 조회
- **POST** `/api/v1/categories?name={name}&description={desc}` - 카테고리 생성
- **PUT** `/api/v1/categories/{id}?name={name}` - 카테고리 수정

#### 🏷️ **태그 API** (TagService)
- **GET** `/api/v1/tags` - 태그 목록
- **POST** `/api/v1/tags` - 태그 생성

#### 💬 **댓글 API** (CommentController, CommentService)
- **POST** `/api/v1/comments?currentUserId={userId}` - 댓글 작성 (인증 필요)
- **GET** `/api/v1/comments/posts/{postId}` - 게시글의 댓글 목록 조회 (계층형)
- **GET** `/api/v1/comments/posts/{postId}/root` - 게시글의 최상위 댓글만 조회
- **GET** `/api/v1/comments/{commentId}` - 댓글 상세 조회
- **GET** `/api/v1/comments/{parentCommentId}/replies` - 대댓글 목록 조회
- **PUT** `/api/v1/comments/{commentId}?currentUserId={userId}` - 댓글 수정 (인증 필요)
- **DELETE** `/api/v1/comments/{commentId}?currentUserId={userId}` - 댓글 삭제 (인증 필요)
- **GET** `/api/v1/comments/author/{authorId}` - 작성자별 댓글 조회
- **GET** `/api/v1/comments/search?keyword={keyword}` - 댓글 검색
- **GET** `/api/v1/comments/recent` - 최근 댓글 조회 (관리자용)
- **POST** `/api/v1/comments/{commentId}/block` - 댓글 차단 (관리자용)
- **POST** `/api/v1/comments/{commentId}/restore` - 댓글 복원 (관리자용)

#### ❤️ **좋아요 API** (PostLikeController, PostLikeService)
- **POST** `/api/v1/posts/{postId}/like?currentUserId={userId}` - 게시글 좋아요 토글 (인증 필요)
- **GET** `/api/v1/posts/{postId}/like/status?currentUserId={userId}` - 좋아요 상태 확인
- **GET** `/api/v1/posts/{postId}/like/count` - 게시글 좋아요 수 조회
- **GET** `/api/v1/posts/{postId}/likes` - 게시글 좋아요한 사용자 목록 조회
- **GET** `/api/v1/posts/likes/me?currentUserId={userId}` - 내가 좋아요한 게시글 목록 조회

#### 📌 **스크랩 API** (PostScrapController, PostScrapService) 
- **POST** `/api/v1/posts/{postId}/scrap?currentUserId={userId}` - 게시글 스크랩 (인증 필요)
- **DELETE** `/api/v1/posts/{postId}/scrap?currentUserId={userId}` - 게시글 스크랩 취소 (인증 필요)
- **GET** `/api/v1/posts/{postId}/scrap/status?currentUserId={userId}` - 스크랩 상태 확인
- **GET** `/api/v1/posts/{postId}/scrap/count` - 게시글 스크랩 수 조회
- **GET** `/api/v1/posts/scraps/me?currentUserId={userId}` - 내 스크랩 목록 조회
- **PUT** `/api/v1/posts/{postId}/scrap/move?currentUserId={userId}&targetFolderId={folderId}` - 스크랩 폴더 이동
- **GET** `/api/v1/posts/scraps/me/search?currentUserId={userId}&keyword={keyword}` - 스크랩 검색
- **GET** `/api/v1/posts/scraps/me/recent?currentUserId={userId}&days={days}` - 최근 스크랩 조회

#### 📁 **스크랩 폴더 API** (ScrapFolderController, ScrapFolderService)
- **POST** `/api/v1/scrap-folders?currentUserId={userId}` - 스크랩 폴더 생성 (인증 필요)
- **GET** `/api/v1/scrap-folders/me?currentUserId={userId}` - 내 스크랩 폴더 목록 조회
- **GET** `/api/v1/scrap-folders/{folderId}?currentUserId={userId}` - 스크랩 폴더 상세 조회
- **PUT** `/api/v1/scrap-folders/{folderId}?currentUserId={userId}` - 스크랩 폴더 수정 (인증 필요)
- **DELETE** `/api/v1/scrap-folders/{folderId}?currentUserId={userId}` - 스크랩 폴더 삭제 (인증 필요)
- **GET** `/api/v1/posts/scrap-folders/{folderId}/scraps?currentUserId={userId}` - 특정 폴더의 스크랩 목록 조회
- **GET** `/api/v1/scrap-folders/me/empty?currentUserId={userId}` - 빈 스크랩 폴더 조회
- **POST** `/api/v1/scrap-folders/{folderId}/set-default?currentUserId={userId}` - 기본 폴더 설정

#### 🎁 **포인트 API** (PointController, PointService) 
- **GET** `/api/v1/points/me?currentUserId={userId}` - 내 포인트 정보 조회
- **GET** `/api/v1/points/me/transactions?currentUserId={userId}` - 내 포인트 거래 내역 조회
- **GET** `/api/v1/points/me/transactions/period?startDate={date}&endDate={date}` - 기간별 거래 내역
- **POST** `/api/v1/points/me/use?currentUserId={userId}` - 포인트 사용 (인증 필요)
- **GET** `/api/v1/points/ranking` - 포인트 랭킹 조회
- **GET** `/api/v1/points/statistics/levels` - 레벨별 사용자 통계
- **GET** `/api/v1/points/statistics/total` - 전체 포인트 통계

#### 🎁 **관리자 포인트 API** 
- **POST** `/api/v1/points/admin/adjust?currentUserId={adminId}` - 포인트 지급/차감 (관리자 전용)
- **GET** `/api/v1/points/admin/users/{userId}` - 사용자 포인트 조회 (관리자 전용)
- **GET** `/api/v1/points/admin/users/level/{level}` - 레벨별 사용자 조회 (관리자 전용)

#### 👑 **역할 및 권한 API** (RoleController, RoleService) 
- **GET** `/api/v1/roles` - 모든 역할 정보 조회
- **GET** `/api/v1/roles/{role}` - 특정 역할 정보 조회
- **GET** `/api/v1/roles/permissions` - 모든 권한 정보 조회
- **GET** `/api/v1/roles/{role}/permissions` - 특정 역할의 권한 목록
- **GET** `/api/v1/roles/check-permission?userId={id}&permission={perm}` - 사용자 권한 확인
- **GET** `/api/v1/roles/statistics` - 역할별 사용자 통계

#### 👑 **관리자 역할 관리 API** 
- **POST** `/api/v1/roles/admin/change?currentUserId={adminId}` - 사용자 역할 변경 (관리자 전용)
- **GET** `/api/v1/roles/admin/{role}/users` - 역할별 사용자 목록 (관리자 전용)
- **GET** `/api/v1/roles/admin/admins` - 관리자 목록 조회 (관리자 전용)

#### 🚨 **신고 API** (ReportController, ReportService) 
- **POST** `/api/v1/reports?currentUserId={userId}` - 신고 생성 (인증 필요)
- **GET** `/api/v1/reports/{reportId}` - 신고 상세 조회
- **GET** `/api/v1/reports/pending` - 대기 중인 신고 목록 (관리자 전용)
- **GET** `/api/v1/reports/high-severity` - 고위험 신고 목록 (관리자 전용)
- **GET** `/api/v1/reports/status/{status}` - 상태별 신고 목록 (관리자 전용)
- **GET** `/api/v1/reports/type/{targetType}` - 대상 유형별 신고 목록 (관리자 전용)
- **GET** `/api/v1/reports/target/{targetType}/{targetId}` - 특정 대상의 신고 목록
- **GET** `/api/v1/reports/me?currentUserId={userId}` - 내가 신고한 목록 조회
- **GET** `/api/v1/reports/user/{userId}` - 특정 사용자에 대한 신고 목록 (관리자 전용)
- **POST** `/api/v1/reports/{reportId}/review/start?currentUserId={adminId}` - 신고 검토 시작 (관리자 전용)
- **POST** `/api/v1/reports/{reportId}/approve?currentUserId={adminId}` - 신고 승인 (관리자 전용)
- **POST** `/api/v1/reports/{reportId}/reject?currentUserId={adminId}` - 신고 반려 (관리자 전용)
- **GET** `/api/v1/reports/statistics` - 신고 통계 조회 (관리자 전용)
- **GET** `/api/v1/reports/statistics/count?startDate={date}&endDate={date}` - 기간별 신고 수 (관리자 전용)
- **GET** `/api/v1/reports/statistics/target/{targetType}/{targetId}/count` - 대상별 신고 수 조회

#### 🔨 **사용자 제재 API** (PenaltyController, UserPenaltyService) 
- **POST** `/api/v1/penalties?currentUserId={adminId}` - 수동 제재 부여 (관리자 전용)
- **GET** `/api/v1/penalties/user/{userId}` - 사용자 제재 이력 조회
- **GET** `/api/v1/penalties/user/{userId}/active` - 사용자 활성 제재 조회
- **GET** `/api/v1/penalties/active` - 모든 활성 제재 목록 (관리자 전용)
- **DELETE** `/api/v1/penalties/{penaltyId}?currentUserId={adminId}` - 제재 해제 (관리자 전용)
- **GET** `/api/v1/penalties/users/{userId}/can-post` - 글쓰기 가능 여부 확인
- **GET** `/api/v1/penalties/users/{userId}/can-comment` - 댓글 작성 가능 여부 확인

#### 🔔 **알림 API** (NotificationController, NotificationService) ✨ NEW!
- **GET** `/api/v1/notifications?currentUserId={userId}` - 내 알림 목록 조회 (인증 필요)
- **GET** `/api/v1/notifications/unread?currentUserId={userId}` - 읽지 않은 알림 목록 조회 (인증 필요)
- **GET** `/api/v1/notifications/unread-count?currentUserId={userId}` - 읽지 않은 알림 개수 조회 (인증 필요)
- **PUT** `/api/v1/notifications/{notificationId}/read?currentUserId={userId}` - 알림 읽음 처리 (인증 필요)
- **PUT** `/api/v1/notifications/read-all?currentUserId={userId}` - 모든 알림 읽음 처리 (인증 필요)
- **DELETE** `/api/v1/notifications/{notificationId}?currentUserId={userId}` - 알림 삭제 (인증 필요)
- **GET** `/api/v1/notifications/preferences?currentUserId={userId}` - 알림 설정 조회 (인증 필요)
- **PUT** `/api/v1/notifications/preferences?currentUserId={userId}` - 알림 설정 변경 (인증 필요)

#### 💊 **Health Check**
- **GET** `/actuator/health` - 서버 상태 확인
- **GET** `/h2-console` - H2 데이터베이스 콘솔

---

## 📋 준비 사항

### 필수 도구
- **curl** 또는 **Postman** (API 테스트용)

### 서버 접속 정보
- **외부 접속 URL**: `http://3.39.36.234:8080`
- **로컬 접속 URL**: `http://localhost:8080`

---

## 🚀 서버 시작하기

### 1. Docker 빌드 및 실행
```bash
cd /home/ec2-user/DDD2

# Docker 이미지 빌드 및 컨테이너 실행
docker-compose up --build -d

# 서버 상태 확인
docker-compose ps
```

### 2. 서버 상태 확인
```bash
# Health Check (로컬)
curl http://localhost:8080/actuator/health

# Health Check (외부)
curl http://3.39.36.234:8080/actuator/health

# 응답 예시:
# {"status":"UP","groups":["liveness","readiness"]}
```

### 3. H2 데이터베이스 콘솔 접속
- **URL**: http://3.39.36.234:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: (비어있음)

---

## 🧪 API 테스트 방법

### 1. 회원가입 테스트

**비밀번호 규칙:**
- 길이: 8~20자
- 필수 포함: 대문자, 소문자, 숫자, 특수문자
- 허용 특수문자: `@ $ ! % * ? &` (주의: `#`는 사용 불가)

```bash
curl -X POST http://3.39.36.234:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123@",
    "nickname": "testuser"
  }'
```

**응답 예시 (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "test@example.com",
    "nickname": "testuser",
    "status": "ACTIVE",
    "createdAt": "2025-12-21 09:33:31",
    "lastLoginAt": null,
    "profile": null
  },
  "message": "회원가입이 완료되었습니다",
  "timestamp": "2025-12-21T09:33:31.856449691"
}
```

### 2. 로그인 테스트

```bash
curl -X POST http://3.39.36.234:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123@"
  }'
```

**응답 예시 (200 OK):**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMkBleGFtcGxlLmNvbSIsInVzZXJJZCI6MSwidG9rZW5UeXBlIjoiQUNDRVNTIiwiYXV0aCI6IlJPTEVfVVNFUiIsImlhdCI6MTc2NjMwOTg3NiwiZXhwIjoxNzY2MzEzNDc2fQ...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMkBleGFtcGxlLmNvbSIsInVzZXJJZCI6MSwidG9rZW5UeXBlIjoiUkVGUkVTSCIsImlhdCI6MTc2NjMwOTg3NiwiZXhwIjoxNzY3NTE5NDc2fQ...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "email": "test@example.com",
      "nickname": "testuser",
      "status": "ACTIVE",
      "createdAt": "2025-12-21 09:37:50",
      "lastLoginAt": null,
      "profile": null
    }
  },
  "message": "로그인이 완료되었습니다",
  "timestamp": "2025-12-21T09:37:56.889171438"
}
```

ADMIN으로 변경하려면 H2 데이터베이스 콘솔에 직접 접속해서 역할을 변경해야 합니다. (최초 관리자는 데이터베이스에서 직접 설정해야 합니다)
```sql
  -- 예: ID가 1인 사용자를 ADMIN으로 변경
  UPDATE users SET role = 'ADMIN' WHERE id = 1;
```

### 3. 인증이 필요한 API 호출 예시

로그인 후 받은 `accessToken`을 사용하여 인증이 필요한 API를 호출할 수 있습니다.

```bash
# 토큰 변수 설정 (로그인 응답에서 받은 토큰)
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 로그아웃 (인증 필요)
curl -X POST http://3.39.36.234:8080/api/v1/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

### 4. 카테고리 생성 및 조회

**카테고리 생성** (관리자용):
```bash
curl -X POST "http://3.39.36.234:8080/api/v1/categories?name=General&description=General%20discussion%20board"
```

**카테고리 트리 조회** (인증 불필요):
```bash
curl http://3.39.36.234:8080/api/v1/categories/tree
```

### 5. 게시글 작성 테스트 (인증 필요) ✨ NEW!

게시글 작성은 로그인한 사용자만 가능합니다. 먼저 로그인하여 토큰을 받아야 합니다.

```bash
# 1. 로그인하여 토큰 받기
TOKEN=$(curl -s -X POST http://3.39.36.234:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123@"}' \
  | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# 2. 게시글 작성 (currentUserId는 로그인한 사용자의 ID)
curl -X POST "http://3.39.36.234:8080/api/v1/posts?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "categoryId": 1,
    "title": "첫 번째 게시글",
    "content": "안녕하세요! 첫 게시글입니다.",
    "contentType": "MARKDOWN",
    "tags": ["테스트", "첫글"]
  }'

# 3. 게시글 발행 (임시저장 → 발행)
curl -X POST "http://3.39.36.234:8080/api/v1/posts/1/publish?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"
```

**응답 예시 (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "첫 번째 게시글",
    "content": "안녕하세요! 첫 게시글입니다.",
    "status": "DRAFT",
    "viewCount": 0,
    "likeCount": 0,
    "commentCount": 0,
    "createdAt": "2025-12-22 12:00:00",
    "author": {
      "id": 1,
      "nickname": "testuser",
      "email": "test@example.com"
    },
    "category": {
      "id": 1,
      "name": "자유게시판"
    }
  },
  "message": "게시글이 작성되었습니다"
}
```

### 6. 게시글 목록 조회 (인증 불필요)

```bash
# 전체 게시글 목록 (페이징)
curl "http://3.39.36.234:8080/api/v1/posts?page=0&size=20"

# 인기 게시글 조회 (최근 7일)
curl "http://3.39.36.234:8080/api/v1/posts/popular?days=7"

# 트렌딩 게시글 조회 (최근 24시간)
curl "http://3.39.36.234:8080/api/v1/posts/trending?hours=24"

# 게시글 검색
curl "http://3.39.36.234:8080/api/v1/posts/search?keyword=테스트"

# 카테고리별 게시글
curl "http://3.39.36.234:8080/api/v1/posts/category/1"
```

### 7. 게시글 상세 조회 (인증 불필요)

```bash
curl http://3.39.36.234:8080/api/v1/posts/1
```

### 8. 댓글 작성 및 조회 (댓글 기능) ✨ NEW!

**댓글 작성 (인증 필요)**
```bash
# 1. 로그인하여 토큰 받기 (위 5번 참고)
TOKEN=$(curl -s -X POST http://3.39.36.234:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123@"}' \
  | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# 2. 최상위 댓글 작성
curl -X POST "http://3.39.36.234:8080/api/v1/comments?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "postId": 1,
    "content": "좋은 게시글이네요! 감사합니다."
  }'

# 3. 대댓글 작성 (parentCommentId 포함)
curl -X POST "http://3.39.36.234:8080/api/v1/comments?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "postId": 1,
    "parentCommentId": 1,
    "content": "저도 동감합니다!"
  }'
```

**응답 예시 (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "postId": 1,
    "parentCommentId": null,
    "author": {
      "id": 1,
      "nickname": "testuser",
      "email": "test@example.com"
    },
    "content": "좋은 게시글이네요! 감사합니다.",
    "status": "ACTIVE",
    "likeCount": 0,
    "depth": 0,
    "createdAt": "2025-12-22 12:00:00",
    "updatedAt": "2025-12-22 12:00:00",
    "replies": [],
    "isLikedByUser": false,
    "isAuthor": true
  },
  "message": "댓글이 작성되었습니다"
}
```

**댓글 목록 조회 (인증 불필요)**
```bash
# 게시글의 모든 댓글 조회 (계층형 구조)
curl "http://3.39.36.234:8080/api/v1/comments/posts/1"

# 게시글의 최상위 댓글만 조회
curl "http://3.39.36.234:8080/api/v1/comments/posts/1/root"

# 특정 댓글의 대댓글 조회
curl "http://3.39.36.234:8080/api/v1/comments/1/replies"

# 댓글 상세 조회
curl "http://3.39.36.234:8080/api/v1/comments/1"
```

**댓글 수정/삭제 (인증 필요)**
```bash
# 댓글 수정
curl -X PUT "http://3.39.36.234:8080/api/v1/comments/1?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "content": "수정된 댓글 내용입니다."
  }'

# 댓글 삭제 (소프트 삭제)
curl -X DELETE "http://3.39.36.234:8080/api/v1/comments/1?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"
```

### 9. 게시글 좋아요 테스트 (좋아요 기능)

**좋아요 토글 (인증 필요)**
```bash
# 1. 로그인하여 토큰 받기 (위 5번 참고)
TOKEN=$(curl -s -X POST http://3.39.36.234:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123@"}' \
  | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# 2. 게시글 좋아요 (첫 번째 호출 시 좋아요 추가)
curl -X POST "http://3.39.36.234:8080/api/v1/posts/1/like?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"

# 3. 게시글 좋아요 다시 호출 (좋아요 취소)
curl -X POST "http://3.39.36.234:8080/api/v1/posts/1/like?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"
```

**응답 예시 (좋아요 추가):**
```json
{
  "success": true,
  "data": {
    "postId": 1,
    "isLiked": true,
    "totalLikeCount": 1,
    "message": "좋아요를 누르셨습니다"
  },
  "message": null,
  "timestamp": "2025-12-22T13:00:00"
}
```

**좋아요 상태 및 정보 조회**
```bash
# 좋아요 상태 확인
curl "http://3.39.36.234:8080/api/v1/posts/1/like/status?currentUserId=1"

# 게시글 좋아요 수 조회
curl "http://3.39.36.234:8080/api/v1/posts/1/like/count"

# 게시글을 좋아요한 사용자 목록 조회
curl "http://3.39.36.234:8080/api/v1/posts/1/likes"

# 내가 좋아요한 게시글 목록 조회
curl "http://3.39.36.234:8080/api/v1/posts/likes/me?currentUserId=1"
```

### 10. 게시글 스크랩 테스트 (스크랩 기능) ✨ NEW!

**스크랩 추가 (인증 필요)**
```bash
# 1. 로그인하여 토큰 받기
TOKEN=$(curl -s -X POST http://3.39.36.234:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123@"}' \
  | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# 2. 게시글 스크랩 (기본 폴더에)
curl -X POST "http://3.39.36.234:8080/api/v1/posts/1/scrap?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "postId": 1,
    "folderId": null
  }'

# 3. 특정 폴더에 스크랩
curl -X POST "http://3.39.36.234:8080/api/v1/posts/2/scrap?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "postId": 2,
    "folderId": 1
  }'
```

**응답 예시 (스크랩 추가):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "post": {
      "id": 1,
      "title": "첫 번째 게시글"
    },
    "scrapFolder": {
      "id": 1,
      "name": "기본 폴더",
      "isDefault": true
    },
    "createdAt": "2025-12-22T15:00:00"
  },
  "message": "게시글이 스크랩되었습니다",
  "timestamp": "2025-12-22T15:00:00"
}
```

**스크랩 취소 및 관리**
```bash
# 스크랩 취소
curl -X DELETE "http://3.39.36.234:8080/api/v1/posts/1/scrap?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"

# 스크랩 상태 확인
curl "http://3.39.36.234:8080/api/v1/posts/1/scrap/status?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"

# 게시글 스크랩 수 조회
curl "http://3.39.36.234:8080/api/v1/posts/1/scrap/count" \
  -H "Authorization: Bearer $TOKEN"

# 내 스크랩 목록 조회
curl "http://3.39.36.234:8080/api/v1/posts/scraps/me?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"

# 스크랩 폴더 이동
curl -X PUT "http://3.39.36.234:8080/api/v1/posts/1/scrap/move?currentUserId=1&targetFolderId=2" \
  -H "Authorization: Bearer $TOKEN"

# 스크랩 검색
curl "http://3.39.36.234:8080/api/v1/posts/scraps/me/search?currentUserId=1&keyword=테스트" \
  -H "Authorization: Bearer $TOKEN"
```

### 11. 스크랩 폴더 관리 테스트 (폴더 기능) ✨ NEW!

**스크랩 폴더 생성 (인증 필요)**
```bash
# 1. 새 스크랩 폴더 생성
curl -X POST "http://3.39.36.234:8080/api/v1/scrap-folders?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "개발 자료",
    "description": "개발 관련 유용한 게시글 모음"
  }'

# 2. 다른 폴더 생성
curl -X POST "http://3.39.36.234:8080/api/v1/scrap-folders?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "디자인 참고",
    "description": "UI/UX 디자인 레퍼런스"
  }'
```

**응답 예시 (폴더 생성):**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "userId": 1,
    "name": "개발 자료",
    "description": "개발 관련 유용한 게시글 모음",
    "isDefault": false,
    "createdAt": "2025-12-22T15:10:00"
  },
  "message": "스크랩 폴더가 생성되었습니다",
  "timestamp": "2025-12-22T15:10:00"
}
```

**스크랩 폴더 관리**
```bash
# 내 스크랩 폴더 목록 조회
curl "http://3.39.36.234:8080/api/v1/scrap-folders/me?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"

# 특정 폴더 상세 조회
curl "http://3.39.36.234:8080/api/v1/scrap-folders/2?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"

# 폴더 수정
curl -X PUT "http://3.39.36.234:8080/api/v1/scrap-folders/2?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "개발 참고자료",
    "description": "백엔드 개발 관련 자료"
  }'

# 특정 폴더의 스크랩 목록 조회
curl "http://3.39.36.234:8080/api/v1/posts/scrap-folders/2/scraps?currentUserId=1"

# 빈 폴더 목록 조회
curl "http://3.39.36.234:8080/api/v1/scrap-folders/me/empty?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"

# 폴더 삭제 (스크랩은 기본 폴더로 이동)
curl -X DELETE "http://3.39.36.234:8080/api/v1/scrap-folders/2?currentUserId=1&moveToDefault=true" \
  -H "Authorization: Bearer $TOKEN"

# 기본 폴더 설정
curl -X POST "http://3.39.36.234:8080/api/v1/scrap-folders/2/set-default?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"
```

### 12. 포인트 시스템 테스트 (포인트 & 레벨) ✨ NEW!

**내 포인트 정보 조회**
```bash
# 1. 로그인하여 토큰 받기
TOKEN=$(curl -s -X POST http://3.39.36.234:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123@"}' \
  | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# 2. 내 포인트 정보 조회

curl "http://3.39.36.234:8080/api/v1/points/me?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"
```

**응답 예시:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "totalPoints": 150,
    "availablePoints": 145,
    "currentLevel": "LEVEL_2",
    "levelDisplayName": "일반",
    "levelNumber": 2,
    "pointsToNextLevel": 350,
    "dailyEarnedPoints": 10,
    "remainingDailyLimit": 90,
    "lastEarnedDate": "2025-12-22",
    "createdAt": "2025-12-22T10:00:00"
  }
}
```

```bash
**특정 사용자 생성**
curl -X POST http://3.39.36.234:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test2@example.com",
    "password": "Password123@",
    "nickname": "testuser2"
  }'


**관리자 포인트 관리 (관리자 전용)**
```bash
# 사용자에게 포인트 지급
curl -X POST "http://3.39.36.234:8080/api/v1/points/admin/adjust?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "targetUserId": 2,
    "points": 100,
    "reason": "이벤트 당첨 보상"
  }'

# 사용자 포인트 차감
curl -X POST "http://3.39.36.234:8080/api/v1/points/admin/adjust?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "targetUserId": 2,
    "points": -50,
    "reason": "부적절한 게시글 작성"
  }'

```bash
# 특정 사용자 포인트 조회
curl "http://3.39.36.234:8080/api/v1/points/admin/users/1" \
  -H "Authorization: Bearer $TOKEN"

# 특정 레벨 이상 사용자 조회
curl "http://3.39.36.234:8080/api/v1/points/admin/users/level/LEVEL_5" \
  -H "Authorization: Bearer $TOKEN"
```


**포인트 거래 내역 및 랭킹**
```bash
# 내 포인트 거래 내역 조회
curl "http://3.39.36.234:8080/api/v1/points/me/transactions?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"

# 기간별 거래 내역
curl "http://3.39.36.234:8080/api/v1/points/me/transactions/period?currentUserId=1&startDate=2025-12-01T00:00:00&endDate=2025-12-31T23:59:59" \
  -H "Authorization: Bearer $TOKEN"

# 포인트 사용
curl -X POST "http://3.39.36.234:8080/api/v1/points/me/use?currentUserId=2" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "points": 50,
    "description": "프로필 배경 이미지 구매"
  }'

# 포인트 랭킹 조회 (상위 50명)
curl "http://3.39.36.234:8080/api/v1/points/ranking?size=50" \
  -H "Authorization: Bearer $TOKEN"

# 레벨별 사용자 통계
curl "http://3.39.36.234:8080/api/v1/points/statistics/levels" \
  -H "Authorization: Bearer $TOKEN"

# 전체 포인트 통계
curl "http://3.39.36.234:8080/api/v1/points/statistics/total" \
  -H "Authorization: Bearer $TOKEN"
```


### 13. 역할 및 권한 관리 테스트 (RBAC) ✨ NEW!

**역할 및 권한 정보 조회 (인증 불필요)**
```bash
# 1. 모든 역할 정보 조회
curl "http://3.39.36.234:8080/api/v1/roles" \
  -H "Authorization: Bearer $TOKEN"

# 2. 특정 역할 상세 정보 조회
curl "http://3.39.36.234:8080/api/v1/roles/MODERATOR" \
  -H "Authorization: Bearer $TOKEN"

# 3. 모든 권한 정보 조회
curl "http://3.39.36.234:8080/api/v1/roles/permissions" \
  -H "Authorization: Bearer $TOKEN"

# 4. 특정 역할의 권한 목록 조회
curl "http://3.39.36.234:8080/api/v1/roles/ADMIN/permissions" \
  -H "Authorization: Bearer $TOKEN"

# 5. 역할별 사용자 통계
curl "http://3.39.36.234:8080/api/v1/roles/statistics" \
  -H "Authorization: Bearer $TOKEN"
```

**응답 예시 (역할 정보):**
```json
{
  "success": true,
  "data": [
    {
      "role": "USER",
      "displayName": "일반 유저",
      "description": "기본 권한을 가진 일반 사용자",
      "minimumLevel": 0,
      "permissions": ["POST_READ", "POST_CREATE", "POST_UPDATE_OWN", "..."],
      "userCount": 150
    },
    {
      "role": "POWER_USER",
      "displayName": "파워 유저",
      "description": "높은 레벨의 활동적인 사용자 (레벨 7+)",
      "minimumLevel": 7,
      "permissions": ["POST_READ", "POST_CREATE", "POST_UPDATE_OWN", "..."],
      "userCount": 25
    },
    {
      "role": "MODERATOR",
      "displayName": "부관리자",
      "description": "콘텐츠 관리 및 신고 처리 권한",
      "minimumLevel": 0,
      "permissions": ["POST_UPDATE_ALL", "POST_DELETE_ALL", "REPORT_MANAGE", "..."],
      "userCount": 5
    }
  ]
}
```

**사용자 권한 확인**
```bash
# 로그인하여 토큰 받기
TOKEN=$(curl -s -X POST http://3.39.36.234:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123@"}' \
  | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# 특정 사용자의 특정 권한 확인
curl "http://3.39.36.234:8080/api/v1/roles/check-permission?userId=2&permission=POST_DELETE_ALL" \
  -H "Authorization: Bearer $TOKEN"
```

**응답 예시 (권한 확인):**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "permission": "POST_DELETE_ALL",
    "hasPermission": false,
    "message": "권한이 없습니다"
  }
}
```

**관리자 역할 관리 (관리자 전용)**
```bash
# 1. 사용자 역할 변경 (관리자만 가능)
curl -X POST "http://3.39.36.234:8080/api/v1/roles/admin/change?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "targetUserId": 2,
    "newRole": "MODERATOR"
  }'

# 2. 특정 역할의 사용자 목록 조회
curl "http://3.39.36.234:8080/api/v1/roles/admin/MODERATOR/users" \
  -H "Authorization: Bearer $TOKEN"

# 3. 관리자 목록 조회
curl "http://3.39.36.234:8080/api/v1/roles/admin/admins" \
  -H "Authorization: Bearer $TOKEN"
```

**응답 예시 (역할 변경 성공):**
```json
{
  "success": true,
  "data": null,
  "message": "역할이 변경되었습니다",
  "timestamp": "2025-12-22T16:00:00"
}
```

**역할 자동 승격 시나리오:**
```bash
# 시나리오: 사용자가 LEVEL 7에 도달하면 자동으로 POWER_USER로 승격
# 1. 사용자 포인트 조회 (현재 LEVEL 6, USER 역할)
curl "http://3.39.36.234:8080/api/v1/points/me?currentUserId=2" \
  -H "Authorization: Bearer $TOKEN"

# → totalPoints: 7500, currentLevel: LEVEL_6

# 2. 관리자가 포인트 지급 (LEVEL 7로 상승)
curl -X POST "http://3.39.36.234:8080/api/v1/points/admin/adjust?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "targetUserId": 2,
    "points": 1000,
    "reason": "활동 보상"
  }'

# 3. 포인트 재조회 (LEVEL 7 달성, 자동으로 POWER_USER로 승격됨)
curl "http://3.39.36.234:8080/api/v1/points/me?currentUserId=2" \
  -H "Authorization: Bearer $TOKEN"

# → totalPoints: 8500, currentLevel: LEVEL_7, role: POWER_USER
```

### 14. 신고 시스템 테스트 (신고 관리) ✨ NEW!

**게시글 신고하기 (인증 필요)**
```bash
# 1. 로그인하여 토큰 받기
TOKEN=$(curl -s -X POST http://3.39.36.234:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123@"}' \
  | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# 2. 게시글 신고 (스팸)
curl -X POST "http://3.39.36.234:8080/api/v1/reports?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "targetType": "POST",
    "targetId": 1,
    "reason": "SPAM",
    "description": "스팸성 광고 게시글입니다."
  }'

# 3. 댓글 신고 (욕설)
curl -X POST "http://3.39.36.234:8080/api/v1/reports?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "targetType": "COMMENT",
    "targetId": 10,
    "reason": "ABUSE",
    "description": "욕설이 포함된 댓글입니다."
  }'

# 4. 사용자 신고 (혐오 발언)
curl -X POST "http://3.39.36.234:8080/api/v1/reports?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "targetType": "USER",
    "targetId": 3,
    "reason": "HATE_SPEECH",
    "description": "지속적으로 혐오 발언을 합니다."
  }'
```

**응답 예시 (신고 생성):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "reporterId": 1,
    "reportedUserId": 2,
    "targetType": "POST",
    "targetId": 5,
    "reason": "SPAM",
    "description": "스팸성 광고 게시글입니다.",
    "status": "PENDING",
    "reviewerId": null,
    "reviewComment": null,
    "reviewedAt": null,
    "actionTaken": null,
    "createdAt": "2025-12-22T18:00:00",
    "updatedAt": "2025-12-22T18:00:00"
  },
  "message": "신고가 접수되었습니다",
  "timestamp": "2025-12-22T18:00:00"
}
```

**신고 조회 및 관리**
```bash
# 신고 상세 조회
curl "http://3.39.36.234:8080/api/v1/reports/1" \
  -H "Authorization: Bearer $TOKEN"

# 내가 신고한 목록 조회
curl "http://3.39.36.234:8080/api/v1/reports/me?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"

# 특정 게시글에 대한 신고 목록 조회
curl "http://3.39.36.234:8080/api/v1/reports/target/POST/1" \
  -H "Authorization: Bearer $TOKEN"

# 특정 댓글에 대한 신고 수 조회
curl "http://3.39.36.234:8080/api/v1/reports/statistics/target/COMMENT/1/count" \
  -H "Authorization: Bearer $TOKEN"
```

**관리자 신고 관리 (관리자 전용)**
```bash
# 1. 대기 중인 신고 목록 조회
curl "http://3.39.36.234:8080/api/v1/reports/pending?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# 2. 고위험 신고 목록 조회 (음란물, 폭력, 혐오발언 등)
curl "http://3.39.36.234:8080/api/v1/reports/high-severity?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# 3. 상태별 신고 목록 조회 (PENDING, IN_REVIEW, APPROVED, REJECTED)
curl "http://3.39.36.234:8080/api/v1/reports/status/PENDING?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# 4. 신고 검토 시작 (관리자 ID: 1)
curl -X POST "http://3.39.36.234:8080/api/v1/reports/1/review/start?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"

# 5. 신고 승인 및 조치
curl -X POST "http://3.39.36.234:8080/api/v1/reports/1/approve?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "reviewComment": "스팸 게시글로 확인되어 삭제 조치했습니다.",
    "actionTaken": "게시글 삭제 + 작성자 경고"
  }'

# 6. 신고 반려
curl -X POST "http://3.39.36.234:8080/api/v1/reports/2/reject?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "reviewComment": "신고 내용이 부적절하여 반려합니다."
  }'

# 7. 신고 통계 조회
curl "http://3.39.36.234:8080/api/v1/reports/statistics" \
  -H "Authorization: Bearer $TOKEN"

# 8. 기간별 신고 수 조회
curl "http://3.39.36.234:8080/api/v1/reports/statistics/count?startDate=2025-12-01T00:00:00&endDate=2025-12-31T23:59:59" \
  -H "Authorization: Bearer $TOKEN"

# 9. 특정 사용자에 대한 신고 목록 조회
curl "http://3.39.36.234:8080/api/v1/reports/user/2?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# 10. 대상 유형별 신고 목록 (POST, COMMENT, USER, CHAT)
curl "http://3.39.36.234:8080/api/v1/reports/type/POST?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

**응답 예시 (신고 통계):**
```json
{
  "success": true,
  "data": {
    "totalReports": 150,
    "pendingReports": 25,
    "inReviewReports": 10,
    "approvedReports": 100,
    "rejectedReports": 15,
    "highSeverityReports": 8
  }
}
```

**신고 사유 종류 (ReportReason):**
- **SPAM**: 스팸/광고 (심각도: 10)
- **ABUSE**: 욕설/비방 (심각도: 50)
- **SEXUAL**: 음란물 (심각도: 100) - 고위험
- **VIOLENCE**: 폭력적 콘텐츠 (심각도: 80) - 고위험
- **HATE_SPEECH**: 혐오 발언 (심각도: 100) - 고위험
- **MISINFORMATION**: 허위 정보 (심각도: 30)
- **COPYRIGHT**: 저작권 침해 (심각도: 20)
- **PERSONAL_INFO**: 개인정보 노출 (심각도: 70) - 고위험
- **ILLEGAL_CONTENT**: 불법 콘텐츠 (심각도: 100) - 고위험
- **ETC**: 기타 (심각도: 5)

**신고 상태 (ReportStatus):**
- **PENDING**: 접수 (신고 접수됨, 검토 대기 중)
- **IN_REVIEW**: 검토중 (관리자가 신고 검토 중)
- **APPROVED**: 승인 (신고 승인, 조치 완료)
- **REJECTED**: 반려 (신고 반려)

**신고 대상 유형 (ReportTargetType):**
- **POST**: 게시글
- **COMMENT**: 댓글
- **CHAT**: 채팅 (미구현)
- **USER**: 사용자

### 15. 사용자 제재 시스템 테스트 (제재 & 패널티) ✨ NEW!

**제재 타입 (PenaltyType):**
- **POST_BAN_24H**: 글쓰기 금지 24시간
- **POST_BAN_7D**: 글쓰기 금지 7일
- **POST_BAN_PERMANENT**: 글쓰기 영구 금지
- **COMMENT_BAN_24H**: 댓글 금지 24시간
- **COMMENT_BAN_7D**: 댓글 금지 7일
- **COMMENT_BAN_PERMANENT**: 댓글 영구 금지
- **FULL_BAN**: 계정 정지

**수동 제재 부여 (관리자 전용)**
```bash
# 1. 관리자 로그인하여 토큰 받기
TOKEN=$(curl -s -X POST http://3.39.36.234:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"Password123@"}' \
  | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# 2. 24시간 글쓰기 금지 제재 부여
curl -X POST "http://3.39.36.234:8080/api/v1/penalties?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "targetUserId": 2,
    "penaltyType": "POST_BAN_24H",
    "reason": "스팸 게시글 작성"
  }'

# 3. 7일 댓글 금지 제재 부여
curl -X POST "http://3.39.36.234:8080/api/v1/penalties?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "targetUserId": 3,
    "penaltyType": "COMMENT_BAN_7D",
    "reason": "악성 댓글 반복"
  }'

# 4. 계정 정지 (영구)
curl -X POST "http://3.39.36.234:8080/api/v1/penalties?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "targetUserId": 4,
    "penaltyType": "FULL_BAN",
    "reason": "고위험 신고 승인 - 음란물"
  }'
```

**응답 예시 (제재 부여):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 2,
    "penaltyType": "POST_BAN_24H",
    "penaltyTypeName": "글쓰기 금지 24시간",
    "reason": "스팸 게시글 작성",
    "startDate": "2025-12-23T10:00:00",
    "endDate": "2025-12-24T10:00:00",
    "isActive": true,
    "isPermanent": false,
    "remainingMillis": 86400000,
    "createdBy": 1,
    "createdAt": "2025-12-23T10:00:00"
  },
  "message": "제재가 부여되었습니다",
  "timestamp": "2025-12-23T10:00:00"
}
```

**제재 조회 및 관리**
```bash
# 사용자의 활성 제재 조회
curl "http://3.39.36.234:8080/api/v1/penalties/user/2/active" \
  -H "Authorization: Bearer $TOKEN"

# 사용자의 모든 제재 이력 조회
curl "http://3.39.36.234:8080/api/v1/penalties/user/2?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# 모든 활성 제재 목록 조회 (관리자)
curl "http://3.39.36.234:8080/api/v1/penalties/active?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# 제재 해제
curl -X DELETE "http://3.39.36.234:8080/api/v1/penalties/1?currentUserId=1" \
  -H "Authorization: Bearer $TOKEN"
```

**제재 확인 API**
```bash
# 글쓰기 가능 여부 확인
curl "http://3.39.36.234:8080/api/v1/penalties/users/2/can-post" \
  -H "Authorization: Bearer $TOKEN"

# 댓글 작성 가능 여부 확인
curl "http://3.39.36.234:8080/api/v1/penalties/users/2/can-comment" \
  -H "Authorization: Bearer $TOKEN"
```

**응답 예시 (제재 확인):**
```json
{
  "success": true,
  "data": {
    "userId": 2,
    "canPost": false,
    "canComment": true,
    "message": "제재 중입니다"
  }
}
```

**제재 중 글쓰기 시도 시 에러:**
```bash
# 제재 중인 사용자가 게시글 작성 시도 - 에러 try catch 문 필요
curl -X POST "http://3.39.36.234:8080/api/v1/posts?currentUserId=2" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "categoryId": 1,
    "title": "테스트",
    "content": "테스트 내용",
    "contentType": "MARKDOWN"
  }'

# 에러 응답
{
  "success": false,
  "message": "글쓰기 제재 중입니다. 게시글을 작성할 수 없습니다.",
  "errorCode": "USER_PENALTY_ERROR",
  "timestamp": "2025-12-23T10:05:00"
}
```

**자동 제재 시나리오:**
```bash
# 시나리오: 신고 3회 승인 → 자동 24시간 글쓰기 금지
# 1. 사용자에 대한 신고 승인 (1회)
curl -X POST "http://3.39.36.234:8080/api/v1/reports/1/approve?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "reviewComment": "스팸 게시글 확인",
    "actionTaken": "게시글 삭제"
  }'

# 2. 동일 사용자에 대한 신고 승인 (2회)
curl -X POST "http://3.39.36.234:8080/api/v1/reports/2/approve?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "reviewComment": "스팸 게시글 확인",
    "actionTaken": "게시글 삭제"
  }'

# 3. 동일 사용자에 대한 신고 승인 (3회) → 자동 24시간 제재
curl -X POST "http://3.39.36.234:8080/api/v1/reports/3/approve?currentUserId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "reviewComment": "스팸 게시글 확인",
    "actionTaken": "게시글 삭제"
  }'

# 4. 활성 제재 확인 → POST_BAN_24H 제재가 자동 부여됨
curl "http://3.39.36.234:8080/api/v1/penalties/user/2/active" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📝 개발 참고사항

### 패키지 구조
```
com.community.platform
├── content          # 콘텐츠 도메인 (게시글, 댓글, 카테고리, 태그)
│   ├── domain
│   ├── application
│   ├── presentation.web
│   ├── infrastructure.persistence
│   ├── dto
│   └── exception
├── user             # 사용자 도메인 
│   ├── domain
│   ├── application  # UserService, AuthService
│   ├── presentation.web  # UserController, AuthController
│   ├── infrastructure.persistence
│   ├── dto
│   └── exception
├── engagement       # 참여 도메인 (좋아요, 스크랩)
├── notification     # 알림 도메인
├── authorization    # 권한 도메인
├── reward           # 보상 도메인
├── moderation       # 중재 도메인 (신고)
└── shared           # 공통 코드
    ├── domain
    ├── security
    └── presentation
```

### 중요 설정 파일
- `build.gradle` - Gradle 빌드 설정 및 컴파일 제외 목록
- `Dockerfile` - Docker 이미지 빌드 설정
- `docker-compose.yml` - 컨테이너 실행 설정
- `application.yml` - Spring Boot 설정

### 다음에 작업할 때 참고
1. **Exception 클래스 import**: `exception` 패키지에서 import 필요
   ```java
   import com.community.platform.user.exception.UserNotFoundException;
   import com.community.platform.content.exception.PostNotFoundException;
   ```

2. **Repository import**: `infrastructure.persistence` 패키지에서 import
   ```java
   import com.community.platform.user.infrastructure.persistence.UserRepository;
   ```

3. **MapStruct 설정**: build.gradle에서 Lombok보다 먼저 처리

4. **PostRepository 특이사항**: PostRepositoryImpl 제외로 인해 PostRepositoryCustom 상속 제거됨
   - PostRepositoryImpl 복구 후 다시 연결 필요

---

## 🎯 로드맵

### ✅ Phase 1: 기본 API (완료 - 2025-12-21)
1. ✅ 회원가입/로그인 테스트 가능

### ✅ Phase 2: 핵심 기능 복구 (완료 - 2025-12-22)
1. ✅ 게시글 CRUD, 검색, 인기글 테스트 가능

### ✅ Phase 3: 댓글 기능 복구 (완료 - 2025-12-22)
1. ✅ 댓글/대댓글 CRUD API 사용 가능
2. ✅ 계층형 댓글 구조 지원
3. ✅ Redis 연동 완료 (AWS ElastiCache)

### ✅ Phase 4: 좋아요 기능 복구 (완료 - 2025-12-22)
1. ✅ 좋아요 토글, 상태 확인, 목록 조회 API 사용 가능
2. ✅ 게시글 좋아요 수 자동 업데이트
3. ✅ 좋아요한 사용자/게시글 목록 조회 기능

### ✅ Phase 5: 스크랩 기능 복구 (완료 - 2025-12-22)
1. ✅ 스크랩 추가/취소, 폴더 관리 API 사용 가능
2. ✅ 스크랩 검색, 폴더 이동, 통계 조회 기능

### ✅ Phase 6-1: 보상 시스템 - 포인트 & 레벨 (완료 - 2025-12-22)
1. ✅ 도메인 모델 설계 (UserPoint, UserLevel, PointTransaction)
2. ✅ 레벨 시스템 구현 (10단계 레벨, 포인트 구간별 분류)
3. ✅ 포인트 적립/차감/사용 기능
4. ✅ 일일 포인트 획득 한도 시스템
5. ✅ 포인트 거래 내역 추적
6. ✅ 포인트 랭킹 및 통계 기능
7. ✅ 관리자 포인트 지급/차감 기능

### ✅ Phase 6-2: RBAC - 역할 기반 접근 제어 (완료 - 2025-12-22)
1. ✅ UserRole enum 구현 (5단계 역할 시스템)
2. ✅ Permission enum 구현 (29개 세부 권한)
3. ✅ User 엔티티에 role 필드 추가
4. ✅ 레벨 기반 자동 역할 승격 (LEVEL 7+ → POWER_USER)
5. ✅ 역할 관리 서비스 및 API
6. ✅ 권한 확인 및 검증 기능
7. ✅ 관리자 역할 변경 기능
8. ⏳ Spring Security 통합 (다음 단계)

### ✅ Phase 6-3: 신고 관리 시스템 (완료 - 2025-12-22) ✨ NEW!
1. ✅ 도메인 모델 설계 (Report, ReportTargetType, ReportStatus, ReportReason)
2. ✅ 신고 생성 및 중복 체크 기능
3. ✅ 신고 대상 유형 (게시글, 댓글, 사용자, 채팅)
4. ✅ 신고 사유 및 심각도 시스템 (9가지 사유, 심각도 점수)
5. ✅ 신고 상태 관리 (접수, 검토중, 승인, 반려)
6. ✅ 고위험 신고 자동 분류 (심각도 70+ 신고)
7. ✅ 관리자 신고 검토 및 처리 기능
8. ✅ 신고 통계 및 리포트 기능
9. ✅ 신고 처리 도메인 이벤트 (생성, 검토시작, 승인, 반려)

**역할 시스템 (5단계):**
- **USER** (일반 유저): 기본 권한 (게시글/댓글 읽기, 작성, 자신의 콘텐츠 수정/삭제, 좋아요, 스크랩, 신고)
- **POWER_USER** (파워 유저): LEVEL 7+ 자동 승격, 일반 유저 권한 포함
- **MODERATOR** (부관리자): 콘텐츠 관리 권한 (모든 게시글/댓글 수정/삭제, 신고 관리, 콘텐츠 필터 관리)
- **ADMIN** (운영자): 시스템 관리 권한 (사용자 관리, 역할 변경, 포인트 관리, 공지 관리, 카테고리 관리, 통계 조회)
- **SUPER_ADMIN** (최고 관리자): 모든 권한 (시스템 전체 제어)

**권한 카테고리 (29개 세부 권한):**
- 게시글: READ, CREATE, UPDATE_OWN, DELETE_OWN, UPDATE_ALL, DELETE_ALL, PIN, RECOMMEND
- 댓글: READ, CREATE, UPDATE_OWN, DELETE_OWN, UPDATE_ALL, DELETE_ALL
- 참여: LIKE_CREATE, SCRAP_CREATE
- 신고: REPORT_CREATE, REPORT_MANAGE
- 사용자 관리: USER_MANAGE, USER_BLOCK, USER_TEMP_BLOCK
- 시스템: ROLE_ASSIGN, POINT_MANAGE, CONTENT_FILTER_MANAGE, NOTICE_MANAGE, CATEGORY_MANAGE, STATISTICS_VIEW
- 특별: ALL (모든 권한)

**레벨 시스템:**
- LEVEL_1 (새싹): 0-99 포인트
- LEVEL_2 (일반): 100-499 포인트
- LEVEL_3 (단골): 500-999 포인트
- LEVEL_4 (열성): 1,000-1,999 포인트
- LEVEL_5 (고수): 2,000-3,999 포인트
- LEVEL_6 (달인): 4,000-7,999 포인트
- LEVEL_7 (명인): 8,000-15,999 포인트
- LEVEL_8 (전설): 16,000-31,999 포인트
- LEVEL_9 (영웅): 32,000-63,999 포인트
- LEVEL_10 (신화): 64,000+ 포인트

**포인트 획득 규칙:**
- 게시글 작성: +10점
- 게시글 발행: +5점
- 댓글 작성: +3점
- 게시글 좋아요 받음: +2점
- 댓글 좋아요 받음: +1점
- 게시글 스크랩 받음: +5점
- 일일 로그인: +5점

**포인트 차감 규칙:**
- 게시글 삭제: -5점
- 댓글 삭제: -2점
- 스팸 패널티: -50점
- 신고 패널티: -100점

---
### 16. 게시판 고도화 기능 테스트 (파일 첨부, Redis 조회수, 게시글 복구) ✨ NEW!

**Phase 2에서 추가된 게시판 고도화 기능:**
1. 게시글 파일 첨부 (이미지, 동영상)
2. Redis 기반 조회수 중복 방지 (IP + User ID, 24시간 TTL)
3. 게시글 소프트 삭제 & 복구 (관리자 전용)

---

#### 16.1 파일 첨부 기능

**파일 저장 방식:**
- ✅ **AWS S3 저장** (현재 설정)
  - 버킷: `test-airgateway`
  - 저장 경로: `posts/attachments/{날짜}/{UUID}.{확장자}`
  - 예시: `posts/attachments/2025/12/23/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg`
  - EC2 IAM Role 기반 인증
- 📁 로컬 파일 시스템 저장 (비활성화)
  - `community.file.storage.type=local`로 변경 시 사용 가능

**지원 파일 타입:**
- **이미지 (IMAGE)**: jpg, jpeg, png, gif, webp (최대 10MB)
- **동영상 (VIDEO)**: mp4, avi, mov, wmv (최대 100MB)

**파일 첨부 업로드 (POST /api/v1/posts/{postId}/attachments)**

```bash
# 게시글에 이미지 파일 첨부 (S3에 저장됨)
curl -X POST 'http://3.39.36.234:8080/api/v1/posts/1/attachments?currentUserId=1' \
  -F 'files=@/path/to/image1.jpg' \
  -F 'files=@/path/to/image2.png'

# 응답 예시
{
  "success": true,
  "data": [
    {
      "id": 1,
      "postId": 1,
      "fileName": "a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
      "originalName": "image1.jpg",
      "fileType": "IMAGE",
      "filePath": "posts/attachments/2025/12/23/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",  // S3 객체 키
      "fileUrl": "https://test-airgateway.s3.ap-northeast-2.amazonaws.com/posts/attachments/2025/12/23/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",  // S3 URL
      "fileSize": 2048576,
      "mimeType": "image/jpeg",
      "displayOrder": 0
    },
    {
      "id": 2,
      "postId": 1,
      "fileName": "b2c3d4e5-f6g7-8901-bcde-fg2345678901.png",
      "originalName": "image2.png",
      "fileType": "IMAGE",
      "filePath": "posts/attachments/2025/12/23/b2c3d4e5-f6g7-8901-bcde-fg2345678901.png",
      "fileUrl": "https://test-airgateway.s3.ap-northeast-2.amazonaws.com/posts/attachments/2025/12/23/b2c3d4e5-f6g7-8901-bcde-fg2345678901.png",
      "fileSize": 1536000,
      "mimeType": "image/png",
      "displayOrder": 1
    }
  ],
  "message": "파일이 업로드되었습니다"
}

# 💡 S3 저장 경로 구조
# test-airgateway/
# └── posts/
#     └── attachments/
#         └── 2025/
#             └── 12/
#                 └── 23/
#                     ├── a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg
#                     └── b2c3d4e5-f6g7-8901-bcde-fg2345678901.png
```

**첨부파일 목록 조회 (GET /api/v1/posts/{postId}/attachments)**

```bash
curl 'http://3.39.36.234:8080/api/v1/posts/1/attachments'

# 응답 예시
{
  "success": true,
  "data": [
    {
      "id": 1,
      "postId": 1,
      "fileName": "a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
      "originalName": "image1.jpg",
      "fileType": "IMAGE",
      "filePath": "posts/attachments/2025/12/23/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
      "fileUrl": "https://test-airgateway.s3.ap-northeast-2.amazonaws.com/posts/attachments/2025/12/23/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
      "fileSize": 2048576,
      "mimeType": "image/jpeg",
      "displayOrder": 0
    }
  ]
}
```

**첨부파일 삭제 (DELETE /api/v1/posts/attachments/{attachmentId})**

```bash
# 작성자만 삭제 가능
curl -X DELETE 'http://3.39.36.234:8080/api/v1/posts/attachments/1?currentUserId=1'

# 응답 예시
{
  "success": true,
  "data": null,
  "message": "첨부파일이 삭제되었습니다"
}

# 권한 없는 사용자가 삭제 시도 시 에러
curl -X DELETE 'http://3.39.36.234:8080/api/v1/posts/attachments/1?currentUserId=2'

{
  "success": false,
  "message": "첨부파일을 삭제할 권한이 없습니다",
  "errorCode": "FORBIDDEN"
}
```

**파일 크기 제한 초과 시 에러 응답:**

```bash
# 11MB 이미지 업로드 시도 (제한: 10MB)
{
  "success": false,
  "message": "이미지 파일 크기는 최대 10MB까지 허용됩니다",
  "errorCode": "VALIDATION_FAILED"
}

# 101MB 동영상 업로드 시도 (제한: 100MB)
{
  "success": false,
  "message": "동영상 파일 크기는 최대 100MB까지 허용됩니다",
  "errorCode": "VALIDATION_FAILED"
}
```

**지원하지 않는 파일 형식 에러:**

```bash
# PDF 파일 업로드 시도
{
  "success": false,
  "message": "지원하지 않는 파일 형식입니다: pdf",
  "errorCode": "VALIDATION_FAILED"
}
```

---

#### 16.2 Redis 기반 조회수 중복 방지

**기존 문제:**
- 페이지를 새로고침할 때마다 조회수가 계속 증가
- 같은 사용자가 여러 번 조회 시 중복 카운트

**개선 사항:**
- Redis Set을 사용하여 24시간 동안 중복 조회 방지
- 로그인 사용자: User ID로 식별
- 비로그인 사용자: IP 주소로 식별
- 24시간 TTL 후 자동 만료

**조회수 증가 테스트 시나리오:**

```bash
# 1. 처음 게시글 조회 (조회수 +1)
curl 'http://3.39.36.234:8080/api/v1/posts/1?currentUserId=1'

# 응답에서 viewCount 확인
{
  "success": true,
  "data": {
    "id": 1,
    "title": "테스트 게시글",
    "viewCount": 1,  # 조회수 증가
    ...
  }
}

# 2. 같은 사용자가 즉시 다시 조회 (조회수 증가 안 함)
curl 'http://3.39.36.234:8080/api/v1/posts/1?currentUserId=1'

# viewCount 변화 없음
{
  "success": true,
  "data": {
    "id": 1,
    "viewCount": 1,  # 그대로 유지
    ...
  }
}

# 3. 다른 사용자가 조회 (조회수 +1)
curl 'http://3.39.36.234:8080/api/v1/posts/1?currentUserId=2'

{
  "success": true,
  "data": {
    "id": 1,
    "viewCount": 2,  # 조회수 증가
    ...
  }
}

# 4. 비로그인 사용자 조회 (IP 기반, 조회수 +1)
curl 'http://3.39.36.234:8080/api/v1/posts/1'

{
  "success": true,
  "data": {
    "id": 1,
    "viewCount": 3,  # 조회수 증가
    ...
  }
}

# 5. 같은 비로그인 사용자가 다시 조회 (같은 IP, 조회수 증가 안 함)
curl 'http://3.39.36.234:8080/api/v1/posts/1'

{
  "success": true,
  "data": {
    "id": 1,
    "viewCount": 3,  # 그대로 유지
    ...
  }
}
```

**Redis 조회 기록 확인 (Redis CLI):**

```bash
# Redis에 접속
redis-cli -h clustercfg.test.mxcsbc.apn2.cache.amazonaws.com -p 6379

# 게시글 1번의 조회 기록 확인
SMEMBERS post:view:1

# 출력 예시:
# 1) "user:1"
# 2) "user:2"
# 3) "ip:192.168.1.100"

# TTL 확인 (24시간 = 86400초)
TTL post:view:1

# 출력 예시: 85234 (남은 시간)
```

---

#### 16.3 게시글 소프트 삭제 & 복구

**소프트 삭제 (Soft Delete):**
- 게시글 삭제 시 실제 DB에서 삭제하지 않고 상태만 DELETED로 변경
- deletedAt (삭제 시각), deletedBy (삭제자 ID) 기록
- 일반 목록 조회 시 제외되지만, 관리자는 삭제된 게시글 조회 가능

**게시글 삭제 (POST /api/v1/posts/{postId}/delete)**

```bash
# 작성자가 자신의 게시글 삭제
curl -X DELETE 'http://3.39.36.234:8080/api/v1/posts/1?currentUserId=1'

# 응답
{
  "success": true,
  "data": null,
  "message": "게시글이 삭제되었습니다"
}

# 삭제 후 게시글 조회 시
curl 'http://3.39.36.234:8080/api/v1/posts/1'

# 에러 응답 (DELETED 상태의 게시글은 조회 불가)
{
  "success": false,
  "message": "게시글을 찾을 수 없습니다",
  "errorCode": "POST_NOT_FOUND"
}
```

**삭제된 게시글 목록 조회 (관리자 전용)**

```bash
# GET /api/v1/posts/deleted
curl 'http://3.39.36.234:8080/api/v1/posts/deleted?currentUserId=1&page=0&size=20'

# 응답 예시
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "삭제된 게시글 제목",
        "content": "삭제된 콘텐츠...",
        "status": "DELETED",
        "deletedAt": "2025-01-15T10:30:00",
        "deletedBy": 1,
        "authorId": 1,
        "viewCount": 150,
        "likeCount": 10,
        "commentCount": 5,
        ...
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 20
  }
}
```

**게시글 복구 (관리자 전용)**

```bash
# POST /api/v1/posts/{postId}/restore
curl -X POST 'http://3.39.36.234:8080/api/v1/posts/1/restore?currentUserId=1'

# 응답
{
  "success": true,
  "data": null,
  "message": "게시글이 복구되었습니다"
}

# 복구 후 게시글 조회 가능
curl 'http://3.39.36.234:8080/api/v1/posts/1'

{
  "success": true,
  "data": {
    "id": 1,
    "title": "복구된 게시글",
    "status": "PUBLISHED",
    "deletedAt": null,
    "deletedBy": null,
    ...
  }
}
```

**복구 에러 케이스:**

```bash
# 1. 삭제되지 않은 게시글 복구 시도
curl -X POST 'http://3.39.36.234:8080/api/v1/posts/2/restore?currentUserId=1'

{
  "success": false,
  "message": "삭제되지 않은 게시글입니다",
  "errorCode": "INVALID_STATE"
}

# 2. 존재하지 않는 게시글 복구 시도
curl -X POST 'http://3.39.36.234:8080/api/v1/posts/999/restore?currentUserId=1'

{
  "success": false,
  "message": "게시글을 찾을 수 없습니다",
  "errorCode": "POST_NOT_FOUND"
}
```

---

#### 16.4 파일 첨부 + 게시글 작성 통합 시나리오

**시나리오: 이미지가 포함된 게시글 작성**

```bash
# 1. 게시글 작성
curl -X POST 'http://3.39.36.234:8080/api/v1/posts?currentUserId=1' \
  -H 'Content-Type: application/json' \
  -d '{
    "categoryId": 1,
    "title": "여행 사진 공유",
    "content": "제주도 여행 사진입니다!",
    "contentType": "MARKDOWN",
    "tagNames": ["여행", "제주도", "사진"]
  }'

# 응답에서 postId 확인 (예: 10)
{
  "success": true,
  "data": {
    "id": 10,
    "title": "여행 사진 공유",
    ...
  }
}

# 2. 게시글에 이미지 첨부
curl -X POST 'http://3.39.36.234:8080/api/v1/posts/10/attachments?currentUserId=1' \
  -F 'files=@/home/user/jeju1.jpg' \
  -F 'files=@/home/user/jeju2.jpg' \
  -F 'files=@/home/user/jeju3.jpg'

# 응답
{
  "success": true,
  "data": [
    {
      "id": 5,
      "postId": 10,
      "originalName": "jeju1.jpg",
      "fileType": "IMAGE",
      "displayOrder": 0,
      ...
    },
    {
      "id": 6,
      "postId": 10,
      "originalName": "jeju2.jpg",
      "fileType": "IMAGE",
      "displayOrder": 1,
      ...
    },
    {
      "id": 7,
      "postId": 10,
      "originalName": "jeju3.jpg",
      "fileType": "IMAGE",
      "displayOrder": 2,
      ...
    }
  ],
  "message": "파일이 업로드되었습니다"
}

# 3. 게시글 발행
curl -X POST 'http://3.39.36.234:8080/api/v1/posts/10/publish?currentUserId=1'

# 4. 게시글 조회 (첨부파일 목록 포함)
curl 'http://3.39.36.234:8080/api/v1/posts/10?currentUserId=1'

{
  "success": true,
  "data": {
    "id": 10,
    "title": "여행 사진 공유",
    "content": "제주도 여행 사진입니다!",
    "viewCount": 1,
    "attachments": [  # 첨부파일 목록
      {
        "id": 5,
        "originalName": "jeju1.jpg",
        "fileUrl": "/api/v1/files/2025/01/15/...",
        "fileType": "IMAGE"
      },
      {
        "id": 6,
        "originalName": "jeju2.jpg",
        "fileUrl": "/api/v1/files/2025/01/15/...",
        "fileType": "IMAGE"
      },
      {
        "id": 7,
        "originalName": "jeju3.jpg",
        "fileUrl": "/api/v1/files/2025/01/15/...",
        "fileType": "IMAGE"
      }
    ],
    ...
  }
}
```

---

#### 16.5 Phase 2 주요 개선 사항 요약

**파일 첨부 시스템:**
- ✅ 이미지 (jpg, jpeg, png, gif, webp) 최대 10MB 지원
- ✅ 동영상 (mp4, avi, mov, wmv) 최대 100MB 지원
- ✅ UUID 기반 고유 파일명 생성으로 충돌 방지
- ✅ 날짜별 디렉토리 자동 생성 (yyyy/MM/dd)
- ✅ 파일 크기 및 확장자 검증
- ✅ 작성자 권한 기반 첨부파일 삭제

**Redis 조회수 중복 방지:**
- ✅ IP + User ID 조합으로 24시간 동안 중복 조회 방지
- ✅ Redis Set을 활용한 고성능 중복 체크
- ✅ 24시간 TTL 자동 만료
- ✅ Redis 장애 시 Fallback 처리 (매번 조회수 증가)

**게시글 소프트 삭제 & 복구:**
- ✅ 소프트 삭제로 데이터 보존 (deletedAt, deletedBy 기록)
- ✅ 삭제된 게시글 목록 조회 (관리자 전용)
- ✅ 삭제된 게시글 복구 기능 (관리자 전용)
- ✅ PostRestoredEvent 발행으로 알림 시스템 연동 준비

---

### 17. 알림 시스템 테스트 (이벤트 기반 실시간 알림) ✨ NEW!

**Phase 3에서 추가된 알림 시스템:**
1. 도메인 이벤트 기반 자동 알림 발송
2. 사용자별 알림 설정 (ON/OFF 가능)
3. 댓글, 좋아요, 스크랩, 신고, 레벨업, 제재 알림
4. 읽음/읽지 않음 상태 관리

---

#### 17.1 알림 타입 (NotificationType)

| 알림 타입 | 설명 | 발송 조건 |
|---|---|---|
| COMMENT_ON_POST | 내 글에 댓글 | 내 게시글에 댓글이 달렸을 때 |
| REPLY_ON_COMMENT | 댓글에 답글 | 내 댓글에 대댓글이 달렸을 때 |
| LIKE_ON_POST | 게시글 좋아요 | 내 게시글에 좋아요가 눌렸을 때 |
| SCRAP_ON_POST | 게시글 스크랩 | 내 게시글이 스크랩되었을 때 |
| REPORT_APPROVED | 신고 승인 | 내가 신고한 콘텐츠가 삭제되었을 때 |
| REPORT_REJECTED | 신고 반려 | 내가 신고한 내용이 반려되었을 때 |
| PENALTY_APPLIED | 제재 통보 | 나에게 제재가 부여되었을 때 |
| PENALTY_EXPIRED | 제재 해제 | 나의 제재가 해제되었을 때 |
| LEVEL_UP | 레벨 업 | 레벨이 상승했을 때 |
| SYSTEM_NOTICE | 시스템 공지 | 시스템 공지 (항상 수신) |

---

#### 17.2 알림 API 엔드포인트

**알림 목록 조회:**
```bash
# 내 알림 목록 조회
GET /api/v1/notifications?currentUserId=1&page=0&size=20

# 읽지 않은 알림만 조회
GET /api/v1/notifications/unread?currentUserId=1&page=0&size=20

# 읽지 않은 알림 개수
GET /api/v1/notifications/unread-count?currentUserId=1
```

**알림 읽음 처리:**
```bash
# 개별 알림 읽음 처리
PUT /api/v1/notifications/{notificationId}/read?currentUserId=1

# 모든 알림 일괄 읽음 처리
PUT /api/v1/notifications/read-all?currentUserId=1

# 알림 삭제
DELETE /api/v1/notifications/{notificationId}?currentUserId=1
```

**알림 설정 관리:**
```bash
# 알림 설정 조회
GET /api/v1/notifications/preferences?currentUserId=1

# 알림 설정 변경
PUT /api/v1/notifications/preferences?currentUserId=1
Content-Type: application/json
{
  "notifyComment": true,
  "notifyReply": true,
  "notifyLike": false,
  "notifyScrap": false,
  "notifyReportResult": true,
  "notifyLevelUp": true,
  "notifyPenalty": true
}
```

---

#### 17.3 알림 발송 시나리오 (자동 이벤트 기반)

**시나리오 1: 댓글 작성 → 자동 알림 발송**

```bash
# 1. 사용자 1이 게시글 작성 및 발행
curl -X POST 'http://3.39.36.234:8080/api/v1/posts?currentUserId=1' \
  -H 'Content-Type: application/json' \
  -d '{"categoryId": 1, "title": "테스트", "content": "댓글 테스트", "contentType": "MARKDOWN"}'

curl -X POST 'http://3.39.36.234:8080/api/v1/posts/20/publish?currentUserId=1'

# 2. 사용자 2가 댓글 작성 (CommentCreatedEvent 자동 발행)
curl -X POST 'http://3.39.36.234:8080/api/v1/posts/20/comments?currentUserId=2' \
  -H 'Content-Type: application/json' \
  -d '{"content": "좋은 글이네요!"}'

# 3. 사용자 1의 알림 확인 (자동으로 알림 생성됨!)
curl 'http://3.39.36.234:8080/api/v1/notifications?currentUserId=1'

# 응답:
{
  "success": true,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "type": "COMMENT_ON_POST",
      "title": "새 댓글",
      "content": "회원님의 게시글에 댓글이 달렸습니다",
      "relatedId": 1,
      "relatedType": "COMMENT",
      "isRead": false,
      "readAt": null,
      "createdAt": "2025-01-15T10:00:00"
    }
  ]
}
```

**시나리오 2: 좋아요 → 자동 알림**

```bash
# 사용자 2가 게시글에 좋아요 (LikeCreatedEvent 발행)
curl -X POST 'http://3.39.36.234:8080/api/v1/posts/20/like?currentUserId=2'

# 사용자 1의 알림 확인
curl 'http://3.39.36.234:8080/api/v1/notifications?currentUserId=1'

# 응답: 좋아요 알림 자동 생성
{
  "success": true,
  "data": [
    {
      "type": "LIKE_ON_POST",
      "title": "좋아요",
      "content": "회원님의 게시글을 좋아합니다",
      "relatedId": 20,
      "relatedType": "POST",
      ...
    }
  ]
}
```

**시나리오 3: 레벨업 → 자동 알림**

```bash
# 포인트 획득으로 레벨업 (UserLevelUpEvent 자동 발행)
# 예: 595점 → 610점으로 증가하여 LEVEL_3 달성

curl 'http://3.39.36.234:8080/api/v1/notifications?currentUserId=1'

# 응답: 레벨업 알림 자동 생성
{
  "success": true,
  "data": [
    {
      "type": "LEVEL_UP",
      "title": "레벨 업!",
      "content": "축하합니다! 단골이(가) 되셨습니다 (포인트: 610)",
      "relatedId": 1,
      "relatedType": "USER",
      ...
    }
  ]
}
```

**시나리오 4: 제재 부여 → 자동 알림**

```bash
# 관리자가 제재 부여 (UserPenaltyCreatedEvent 발행)
curl -X POST 'http://3.39.36.234:8080/api/v1/penalties?currentUserId=1' \
  -H 'Content-Type: application/json' \
  -d '{
    "targetUserId": 2,
    "penaltyType": "POST_BAN_24H",
    "reason": "부적절한 게시물"
  }'

# 사용자 2의 알림 확인
curl 'http://3.39.36.234:8080/api/v1/notifications?currentUserId=2'

# 응답: 제재 알림 자동 생성
{
  "success": true,
  "data": [
    {
      "type": "PENALTY_APPLIED",
      "title": "제재 통보",
      "content": "커뮤니티 규칙 위반으로 글쓰기 금지 24시간 제재되었습니다",
      "relatedId": 1,
      "relatedType": "PENALTY",
      ...
    }
  ]
}
```

---

#### 17.4 알림 설정으로 특정 알림 차단

```bash
# 좋아요 알림을 받고 싶지 않은 경우
curl -X PUT 'http://3.39.36.234:8080/api/v1/notifications/preferences?currentUserId=1' \
  -H 'Content-Type: application/json' \
  -d '{
    "notifyComment": true,
    "notifyReply": true,
    "notifyLike": false,
    "notifyScrap": true,
    "notifyReportResult": true,
    "notifyLevelUp": true,
    "notifyPenalty": true
  }'

# 이후 좋아요를 받아도 알림이 생성되지 않음
curl -X POST 'http://3.39.36.234:8080/api/v1/posts/20/like?currentUserId=3'

curl 'http://3.39.36.234:8080/api/v1/notifications?currentUserId=1'
# 좋아요 알림 없음 (설정에서 OFF했기 때문)
```

---

#### 17.5 Phase 3 구현 상세

**이벤트 기반 아키텍처:**
- ✅ `@TransactionalEventListener(phase = AFTER_COMMIT)` 사용
- ✅ 트랜잭션 커밋 후 이벤트 처리로 안정성 보장
- ✅ 자기 자신에게는 알림 발송 안 함

**도메인 이벤트:**
- ✅ `CommentCreatedEvent` - Comment 엔티티에서 발행
- ✅ `LikeCreatedEvent` - PostLike 엔티티에서 발행
- ✅ `ScrapCreatedEvent` - PostScrap 엔티티에서 발행
- ✅ `UserLevelUpEvent` - UserPoint 엔티티에서 발행
- ✅ `UserPenaltyCreatedEvent`, `UserPenaltyExpiredEvent` - Phase 1에서 이미 구현

**엔티티 변경:**
- ✅ Comment: BaseEntity → AggregateRoot
- ✅ PostLike: BaseEntity → AggregateRoot
- ✅ PostScrap: BaseEntity → AggregateRoot
- ✅ UserPoint: BaseEntity → AggregateRoot

**통합:**
- ✅ Phase 1 제재 시스템과 연동 (제재 알림)
- ✅ Phase 2 게시판 고도화와 연동 (댓글, 좋아요, 스크랩 알림)
- ✅ 포인트/레벨 시스템과 연동 (레벨업 알림)
- ✅ 신고 시스템과 연동 (신고 결과 알림)

---

## 🎉 전체 3단계 구현 완료!

### ✅ Phase 1: 사용자 제재 & 패널티 시스템
- 자동 제재 (3회 → 24시간, 5회 → 7일 금지)
- 고위험 신고 즉시 조치
- 제재 스케줄러 (5분마다 자동 만료)
- 7가지 제재 타입

### ✅ Phase 2: 게시판 고도화
- 파일 첨부 (이미지 10MB, 동영상 100MB)
- Redis 조회수 중복 방지 (IP + User, 24시간 TTL)
- 게시글 소프트 삭제 & 관리자 복구

### ✅ Phase 3: 알림 시스템
- 이벤트 기반 자동 알림 발송
- 10가지 알림 타입
- 사용자별 알림 설정
- 읽음/미읽음 상태 관리

**완벽한 통합:**
모든 기능이 도메인 이벤트를 통해 연결되어 있으며, 사용자 행동에 따라 자동으로 알림이 발송됩니다!

