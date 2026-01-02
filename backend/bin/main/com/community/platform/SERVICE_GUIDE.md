# Spring Service 계층 구현 가이드

## 📋 구현된 Service 목록

### 1. User Domain Services
- **UserService**: 사용자 생명주기 관리 (회원가입, 로그인, 프로필 관리)
- **UserEventHandler**: 사용자 도메인 이벤트 처리

### 2. Content Domain Services  
- **PostService**: 게시글 CRUD 및 검색, 통계
- **CommentService**: 댓글/대댓글 계층형 관리
- **CategoryService**: 카테고리 트리 구조 관리
- **TagService**: 태그 생성, 인기도 관리, 추천
- **PostEventHandler**: 게시글 도메인 이벤트 처리

### 3. Engagement Domain Services
- **PostLikeService**: 좋아요/좋아요 취소 및 통계
- **PostScrapService**: 스크랩 추가/제거, 폴더 관리
- **ScrapFolderService**: 스크랩 폴더 생성/수정/삭제

### 4. Shared Services
- **DomainEventService**: 도메인 이벤트 발행 관리
- **DomainEventPublisher**: Spring Event 기반 이벤트 발행

## 🎯 Service 계층 설계 원칙

### 1. DDD Application Service Pattern
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본 읽기 전용
public class UserService {
    
    @Transactional // 쓰기 작업만 별도 지정
    public User registerUser(String email, String password, String nickname) {
        // 비즈니스 규칙 검증
        validateEmailNotDuplicated(email);
        
        // 도메인 객체 생성 및 저장
        User user = User.register(email, encryptedPassword, nickname);
        User savedUser = userRepository.save(user);
        
        // 도메인 이벤트 발행
        domainEventService.publishEvents(savedUser);
        
        return savedUser;
    }
}
```

### 2. 메서드별 한줄 주석 패턴
```java
/**
 * 새 사용자 회원가입 처리
 */
@Transactional
public User registerUser(String email, String password, String nickname) {
    log.info("사용자 회원가입 처리 시작. email: {}, nickname: {}", email, nickname);
    
    // 이메일 중복 체크
    validateEmailNotDuplicated(email);
    
    // 닉네임 중복 체크  
    validateNicknameNotDuplicated(nickname);
    
    log.info("사용자 회원가입 완료. userId: {}", savedUser.getId());
    return savedUser;
}
```

### 3. 예외 처리 전략
```java
// 비즈니스 예외 정의
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(Long userId) {
        super("USER_NOT_FOUND", "사용자를 찾을 수 없습니다. ID: " + userId);
    }
}

// 서비스에서 활용
public User getUserById(Long userId) {
    return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
}
```

## 🔄 도메인 이벤트 처리 패턴

### 1. 이벤트 발행 (Service에서)
```java
@Transactional
public User registerUser(String email, String password, String nickname) {
    // 비즈니스 로직 수행
    User user = User.register(email, encryptedPassword, nickname);
    User savedUser = userRepository.save(user);
    
    // 도메인 이벤트 발행
    domainEventService.publishEvents(savedUser);
    
    return savedUser;
}
```

### 2. 이벤트 처리 (EventHandler에서)
```java
@Service
public class UserEventHandler {
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            // 후속 처리 (기본 폴더 생성, 알림 등)
            scrapFolderService.createDefaultFolder(event.getUserId());
        } catch (Exception e) {
            log.error("후속 처리 실패. 메인 로직에 영향 없음", e);
            // 예외를 삼켜서 메인 비즈니스 로직에 영향을 주지 않음
        }
    }
}
```

## 💡 서비스 메서드 패턴

### 1. CRUD 패턴
```java
// 생성 (Create)
@Transactional
public Post createPost(Long authorId, Long categoryId, String title, String content) {
    // 검증 로직
    validateUserExists(authorId);
    
    // 도메인 객체 생성
    Post post = Post.create(authorId, category, title, content, contentType);
    
    // 저장 및 후속 처리
    Post savedPost = postRepository.save(post);
    domainEventService.publishEvents(savedPost);
    
    return savedPost;
}

// 조회 (Read)
public Post getPostById(Long postId) {
    return postRepository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));
}

// 수정 (Update)  
@Transactional
public void updatePost(Long postId, Long authorId, String title, String content) {
    Post post = getPostById(postId);
    validateAuthorPermission(post, authorId);
    
    post.updateContent(title, content);
    domainEventService.publishEvents(post);
}

// 삭제 (Delete)
@Transactional
public void deletePost(Long postId, Long authorId) {
    Post post = getPostById(postId);
    validateAuthorPermission(post, authorId);
    
    post.delete(); // 소프트 삭제
    domainEventService.publishEvents(post);
}
```

### 2. 상태 변경 패턴
```java
@Transactional
public boolean togglePostLike(Long userId, Long postId) {
    boolean isLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
    
    if (isLiked) {
        removePostLike(userId, postId);
        return false; // 좋아요 취소
    } else {
        addPostLike(userId, postId);
        return true; // 좋아요 추가
    }
}
```

### 3. 검색/목록 조회 패턴
```java
// 단순 목록 조회
public Page<Post> getPublishedPosts(Pageable pageable) {
    return postRepository.findByStatusOrderByPublishedAtDesc(PostStatus.PUBLISHED, pageable);
}

// 복합 검색 (QueryDSL 활용)
public Page<Post> searchPostsWithFilters(String keyword, Long categoryId, 
                                       List<String> tagNames, Pageable pageable) {
    return postRepository.searchPostsWithFilters(
            keyword, categoryId, tagNames, PostStatus.PUBLISHED, 
            null, null, pageable);
}

// 통계 조회
public Long getPostCountByAuthor(Long authorId) {
    return postRepository.countByAuthorIdAndStatus(authorId, PostStatus.PUBLISHED);
}
```

### 4. 검증 메서드 패턴
```java
// private 검증 메서드들
private void validateUserExists(Long userId) {
    if (!userRepository.existsById(userId)) {
        throw new UserNotFoundException(userId);
    }
}

private void validateAuthorPermission(Post post, Long userId) {
    if (!post.getAuthorId().equals(userId)) {
        throw new IllegalArgumentException("게시글 작성자만 수정/삭제할 수 있습니다.");
    }
}

private void validateEmailNotDuplicated(String email) {
    if (userRepository.existsByEmail(email)) {
        throw new DuplicateUserException("이메일", email);
    }
}
```

## 📊 로깅 전략

### 1. 로깅 레벨별 활용
```java
// INFO: 비즈니스 시작/완료 로그
log.info("사용자 회원가입 처리 시작. email: {}, nickname: {}", email, nickname);
log.info("사용자 회원가입 완료. userId: {}", savedUser.getId());

// DEBUG: 상세 처리 과정
log.debug("게시글 생성 후속 처리 완료. postId: {}", event.getPostId());

// WARN: 비정상적이지만 처리 가능한 상황
log.warn("사용자 차단 이벤트. userId: {}, reason: {}", event.getUserId(), event.getReason());

// ERROR: 오류 상황
log.error("게시글 발행 후속 처리 실패. postId: {}, error: {}", event.getPostId(), e.getMessage(), e);
```

## 🚀 다음 단계

Service 계층이 완료되었으므로 이제 다음과 같은 순서로 진행할 수 있습니다:

1. **DTO & Mapper**: API 요청/응답 객체 설계
2. **Controller**: REST API 엔드포인트 구현  
3. **Security**: Spring Security 인증/인가 설정
4. **Validation**: Bean Validation 적용
5. **Test**: 단위/통합 테스트 작성

각 서비스는 독립적으로 테스트 가능하도록 설계되었으며, 도메인 이벤트를 통한 느슨한 결합을 통해 확장성을 확보했습니다.