# Spring Data JPA Repository 구현 가이드

## 📋 구현된 Repository 목록

### 1. User Domain
- **UserRepository**: 사용자 기본 CRUD 및 검색 기능
- **UserProfileRepository**: 사용자 프로필 관리

### 2. Content Domain  
- **PostRepository**: 게시글 CRUD + 커스텀 쿼리 (QueryDSL)
- **CommentRepository**: 댓글 계층형 구조 관리
- **CategoryRepository**: 카테고리 트리 구조 관리
- **TagRepository**: 태그 시스템 및 인기도 관리
- **PostTagRepository**: 게시글-태그 연결 관리

### 3. Engagement Domain
- **PostLikeRepository**: 게시글 좋아요 관리
- **PostScrapRepository**: 게시글 스크랩 관리  
- **ScrapFolderRepository**: 스크랩 폴더 관리

## 🎯 Repository 메서드 패턴

### 기본 CRUD 패턴
```java
// 조회
Optional<Entity> findById(Long id);
List<Entity> findAll();
Page<Entity> findAll(Pageable pageable);

// 저장
Entity save(Entity entity);
List<Entity> saveAll(List<Entity> entities);

// 삭제
void deleteById(Long id);
void delete(Entity entity);
```

### 조건부 조회 패턴
```java
// 단일 조건 조회
Optional<User> findByEmail(String email);
List<Post> findByStatus(PostStatus status);

// 복합 조건 조회
Page<Post> findByStatusAndCategoryId(PostStatus status, Long categoryId, Pageable pageable);

// 존재 여부 확인
boolean existsByEmail(String email);
boolean existsByUserIdAndPostId(Long userId, Long postId);
```

### 통계/집계 패턴
```java
// 개수 조회
@Query("SELECT COUNT(p) FROM Post p WHERE p.status = :status")
Long countByStatus(@Param("status") PostStatus status);

// 기간별 통계
Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
```

### 업데이트 패턴
```java
// 벌크 업데이트
@Modifying
@Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
void incrementViewCount(@Param("postId") Long postId);
```

## 🔧 QueryDSL 커스텀 Repository

### 구현 단계
1. **인터페이스 정의**: `PostRepositoryCustom`
2. **구현체 작성**: `PostRepositoryImpl` 
3. **원본 Repository 확장**: `extends PostRepositoryCustom`

### 커스텀 쿼리 사용 예시
```java
// 복합 검색
Page<Post> searchPostsWithFilters(
    String keyword,           // 키워드 검색
    Long categoryId,          // 카테고리 필터  
    List<String> tagNames,    // 태그 필터
    PostStatus status,        // 상태 필터
    LocalDateTime startDate,  // 시작일
    LocalDateTime endDate,    // 종료일
    Pageable pageable
);

// 인기도 기반 정렬
Page<Post> findPopularPostsWithScore(
    PostStatus status,
    LocalDateTime fromDate, 
    Pageable pageable
);
```

## 💡 성능 최적화 팁

### 1. N+1 문제 방지
```java
// JOIN FETCH 사용
@Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.id = :postId")
Optional<Post> findByIdWithCategory(@Param("postId") Long postId);

// @EntityGraph 사용 (선택적)
@EntityGraph(attributePaths = {"category", "postTags.tag"})
List<Post> findByStatus(PostStatus status);
```

### 2. 페이징 최적화
```java
// 커서 기반 페이징 (대량 데이터용)
@Query("SELECT p FROM Post p WHERE p.id < :lastId ORDER BY p.id DESC")
List<Post> findNextPosts(@Param("lastId") Long lastId, Pageable pageable);
```

### 3. 벌크 연산 활용
```java
// 대량 업데이트
@Modifying
@Query("UPDATE Post p SET p.status = :newStatus WHERE p.authorId = :authorId")
void updateStatusByAuthorId(@Param("authorId") Long authorId, @Param("newStatus") PostStatus newStatus);
```

### 4. 인덱스 활용
```java
// 복합 인덱스 활용한 정렬
Page<Post> findByStatusOrderByPublishedAtDesc(PostStatus status, Pageable pageable);
```

## 📊 Repository 메서드 한줄 설명 규칙

### 명명 규칙
- **조회**: `findBy`, `getBy`, `queryBy`
- **존재확인**: `existsBy`  
- **개수**: `countBy`
- **삭제**: `deleteBy`, `removeBy`

### 주석 작성 패턴
```java
// {목적} ({사용처})
Optional<User> findByEmail(String email); // 이메일로 사용자 조회 (로그인 시 사용)
boolean existsByEmail(String email);      // 이메일 존재 여부 확인 (회원가입 시 중복 체크)
Long countByAuthorId(Long authorId);      // 작성자별 게시글 개수 조회 (프로필 페이지용)
```

## 🚀 다음 단계

1. **Service 계층** 구현
2. **DTO/Mapper** 설정  
3. **Controller** API 엔드포인트 구현
4. **테스트 코드** 작성
5. **캐시 적용** (Redis)

이제 Repository 계층이 완성되었으므로, 비즈니스 로직을 담당하는 Service 계층 구현을 시작할 수 있습니다!