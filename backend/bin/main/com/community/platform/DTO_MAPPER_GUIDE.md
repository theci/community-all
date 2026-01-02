# DTO & Mapper 구현 가이드

## 📋 구현 완료된 DTO 목록

### 1. 공통 DTO
- **ApiResponse<T>**: 통일된 API 응답 형식
- **PageResponse<T>**: 페이징 응답 형식  
- **ValidationErrorResponse**: 유효성 검사 오류 응답

### 2. User Domain DTOs
**Request DTOs:**
- `UserRegisterRequest`: 회원가입 (이메일, 비밀번호, 닉네임 검증)
- `UserLoginRequest`: 로그인 (이메일, 비밀번호)
- `UserProfileUpdateRequest`: 프로필 수정 (이미지, 자기소개, 생년월일, 성별)

**Response DTOs:**
- `UserResponse`: 완전한 사용자 정보 (프로필 포함)
- `UserSummaryResponse`: 요약 사용자 정보 (작성자 표시용)
- `UserProfileResponse`: 프로필 정보

### 3. Content Domain DTOs
**Request DTOs:**
- `PostCreateRequest`: 게시글 작성 (카테고리, 제목, 내용, 태그)
- `PostUpdateRequest`: 게시글 수정 (제목, 내용, 태그)
- `PostSearchRequest`: 게시글 검색 (키워드, 카테고리, 태그, 날짜)
- `CommentCreateRequest`: 댓글 작성 (게시글ID, 부모댓글ID, 내용)
- `CommentUpdateRequest`: 댓글 수정 (내용)

**Response DTOs:**
- `PostResponse`: 완전한 게시글 정보 (태그, 상호작용 포함)
- `PostSummaryResponse`: 게시글 요약 (목록용, 내용 100자 요약)
- `CommentResponse`: 댓글 정보 (대댓글 포함)
- `CategoryResponse`: 카테고리 정보 (하위 카테고리, 게시글 수)
- `TagResponse`: 태그 정보 (이름, 색상, 사용횟수)

### 4. Engagement Domain DTOs
**Request DTOs:**
- `PostScrapRequest`: 게시글 스크랩 (게시글ID, 폴더ID)
- `ScrapFolderCreateRequest`: 스크랩 폴더 생성 (이름, 설명)
- `ScrapFolderUpdateRequest`: 스크랩 폴더 수정 (이름, 설명)

**Response DTOs:**
- `PostLikeResponse`: 좋아요 정보 (사용자, 시간)
- `PostScrapResponse`: 스크랩 정보 (게시글, 폴더)
- `ScrapFolderResponse`: 스크랩 폴더 (스크랩 수 포함)
- `LikeToggleResponse`: 좋아요 토글 결과 (상태, 총 개수, 메시지)

## 🎯 DTO 설계 패턴

### 1. 공통 응답 형식
```java
// 성공 응답
{
    "success": true,
    "data": { /* 실제 데이터 */ },
    "message": "성공적으로 처리되었습니다",
    "timestamp": "2024-01-01T12:00:00"
}

// 실패 응답  
{
    "success": false,
    "message": "오류가 발생했습니다",
    "errorCode": "USER_NOT_FOUND",
    "timestamp": "2024-01-01T12:00:00"
}
```

### 2. 페이징 응답 형식
```java
{
    "success": true,
    "data": {
        "content": [ /* 실제 데이터 배열 */ ],
        "pageInfo": {
            "currentPage": 0,
            "totalPages": 10,
            "totalElements": 100,
            "pageSize": 10,
            "hasNext": true,
            "hasPrevious": false,
            "isFirst": true,
            "isLast": false
        }
    }
}
```

### 3. Bean Validation 활용
```java
@NotBlank(message = "이메일은 필수입니다")
@Email(message = "올바른 이메일 형식이 아닙니다")
@Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다")
private String email;

@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])", 
         message = "비밀번호는 대소문자, 숫자, 특수문자를 포함해야 합니다")
private String password;
```

## 🗺️ MapStruct Mapper 패턴

### 1. 기본 매핑
```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    
    // 단순 매핑
    UserResponse toUserResponse(User user);
    
    // 리스트 매핑
    List<UserResponse> toUserResponseList(List<User> users);
}
```

### 2. 복합 매핑 (연관 관계 포함)
```java
// 사용자와 프로필을 함께 매핑
default UserResponse toUserResponseWithProfile(User user, UserProfile profile) {
    UserResponse.UserResponseBuilder builder = UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .status(user.getStatus());

    if (profile != null) {
        builder.profile(toUserProfileResponse(profile));
    }

    return builder.build();
}
```

### 3. 조건부 매핑 (사용자별 상호작용)
```java
// 게시글과 사용자별 좋아요/스크랩 상태 함께 매핑
default PostResponse toPostResponseWithDetails(Post post, UserSummaryResponse author, 
                                             CategoryResponse category, List<TagResponse> tags,
                                             Boolean isLikedByUser, Boolean isScrappedByUser) {
    return PostResponse.builder()
            .id(post.getId())
            .author(author)
            .category(category)
            .title(post.getTitle())
            .content(post.getContent())
            .tags(tags)
            .isLikedByUser(isLikedByUser)    // 사용자별 상태
            .isScrappedByUser(isScrappedByUser) // 사용자별 상태
            .build();
}
```

## 📊 실무 적용 사례

### 1. 게시글 목록 API 응답
```java
// PostSummaryResponse - 목록용 (성능 최적화)
{
    "id": 1,
    "author": {
        "id": 100,
        "nickname": "user123",
        "profileImageUrl": "https://..."
    },
    "title": "제목",
    "summary": "내용 요약 100자...",
    "viewCount": 1500,
    "likeCount": 25,
    "commentCount": 8,
    "publishedAt": "2024-01-01T12:00:00",
    "tags": [
        {"id": 1, "name": "Spring", "color": "#6DB33F"},
        {"id": 2, "name": "Java", "color": "#ED8B00"}
    ],
    "isLikedByUser": false,
    "isScrappedByUser": true
}
```

### 2. 댓글 계층 구조 응답
```java
// CommentResponse - 대댓글 포함
{
    "id": 1,
    "postId": 100,
    "parentCommentId": null,
    "author": { /* 작성자 정보 */ },
    "content": "댓글 내용",
    "depth": 0,
    "replies": [
        {
            "id": 2,
            "parentCommentId": 1,
            "content": "대댓글 내용",
            "depth": 1,
            "replies": []
        }
    ]
}
```

### 3. 유효성 검사 오류 응답
```java
// ValidationErrorResponse
{
    "success": false,
    "message": "입력 값이 올바르지 않습니다",
    "errorCode": "VALIDATION_ERROR",
    "data": {
        "message": "유효성 검사 실패",
        "fieldErrors": [
            {
                "field": "email",
                "rejectedValue": "invalid-email",
                "message": "올바른 이메일 형식이 아닙니다"
            },
            {
                "field": "password",
                "rejectedValue": "123",
                "message": "비밀번호는 8~20자여야 합니다"
            }
        ]
    }
}
```

## 🔧 성능 최적화 고려사항

### 1. 응답 크기 최적화
- **Summary DTO**: 목록용 간소화된 정보
- **@JsonInclude(NON_NULL)**: null 필드 제외
- **태그 제한**: 목록에서는 최대 3개만 표시

### 2. N+1 문제 방지
- Mapper에서 복합 정보는 별도 메서드로 처리
- 서비스 계층에서 연관 데이터를 미리 조회

### 3. 사용자별 정보 처리
- 로그인한 사용자의 좋아요/스크랩 상태
- 작성자 여부에 따른 수정/삭제 권한 표시

## 🚀 다음 단계

DTO & Mapper가 완성되었으므로 이제 다음과 같은 순서로 진행할 수 있습니다:

1. **Controller**: REST API 엔드포인트 구현
2. **Exception Handler**: 전역 예외 처리
3. **Security**: Spring Security 인증/인가
4. **Validation**: 커스텀 검증 로직
5. **Test**: API 테스트 작성

모든 DTO는 실제 API 요구사항을 고려하여 설계되었으며, 성능과 사용성을 모두 고려한 구조입니다.