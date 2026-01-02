# 커뮤니티 플랫폼 DDD 설계

## 바운디드 컨텍스트 구조

### 🎯 User Management (사용자 관리)
- **목적**: 사용자 생명주기, 인증, 프로필 관리
- **핵심 애그리거트**: User
- **주요 엔티티**: User, UserProfile, UserRole, UserFollowing

### 📝 Content Management (컨텐츠 관리)
- **목적**: 게시글, 댓글, 카테고리, 태그 관리
- **핵심 애그리거트**: Post
- **주요 엔티티**: Post, Comment, Category, Tag, PostTag

### ❤️ Engagement (참여/상호작용)
- **목적**: 좋아요, 스크랩, 조회수 등 사용자 참여 활동
- **핵심 애그리거트**: UserEngagement
- **주요 엔티티**: PostLike, CommentLike, PostScrap, ScrapFolder, PostView

### 🏆 Reward System (보상 시스템)
- **목적**: 포인트, 레벨, 뱃지 관리
- **핵심 애그리거트**: UserReward
- **주요 엔티티**: UserPoint, PointHistory, UserLevel, Badge, UserBadge

### 🔔 Notification (알림)
- **목적**: 실시간 알림 및 알림 설정 관리
- **핵심 애그리거트**: Notification
- **주요 엔티티**: Notification, NotificationTemplate, UserNotificationSetting

### 🔐 Authorization (인가)
- **목적**: 메뉴, 권한, 역할 기반 접근 제어
- **핵심 애그리거트**: Permission
- **주요 엔티티**: Menu, Permission, RolePermission

## 프로덕션급 고려사항

### 성능 최적화
1. **읽기 성능**: 좋아요수, 댓글수, 조회수 등은 별도 캐시 컬럼으로 관리
2. **페이징**: 커서 기반 페이징 지원 (offset 방식의 성능 문제 해결)
3. **인덱싱**: 주요 검색 패턴에 맞는 복합 인덱스 설계

### 확장성
1. **파티셔닝**: 게시글, 댓글 테이블의 날짜 기반 파티셔닝
2. **샤딩**: 사용자 ID 기반 수평 분할 고려
3. **CQRS**: 읽기/쓰기 분리로 성능 최적화

### 데이터 일관성
1. **이벤트 소싱**: 중요한 비즈니스 이벤트 추적
2. **사가 패턴**: 분산 트랜잭션 관리
3. **최종 일관성**: 비동기 처리가 가능한 영역 식별

### 보안
1. **민감정보**: 비밀번호, 개인정보 암호화
2. **감사**: 중요 작업의 감사 로그
3. **레이트 리미팅**: API 호출 제한

### 운영
1. **소프트 딜리트**: 물리적 삭제 대신 상태 변경
2. **버전 관리**: 스키마 변경 이력 관리
3. **모니터링**: 성능 지표 및 비즈니스 메트릭 수집