# 커뮤니티 플랫폼 - DDD 기반 설계

## 🎯 프로젝트 개요

Domain-Driven Design(DDD) 원칙을 적용하여 설계된 프로덕션급 커뮤니티 플랫폼입니다.
헥사고날 아키텍처와 CQRS 패턴을 적용하여 확장 가능하고 유지보수하기 쉬운 구조로 설계되었습니다.

## 🏗️ 아키텍처 개요

### 바운디드 컨텍스트

```
🎯 User Management     - 사용자 생명주기 관리
📝 Content Management  - 게시글, 댓글, 카테고리 관리
❤️ Engagement         - 좋아요, 스크랩, 조회수 관리
🏆 Reward System      - 포인트, 레벨, 뱃지 시스템
🔔 Notification       - 실시간 알림 시스템
🔐 Authorization      - 역할 기반 권한 관리
```

### 프로젝트 구조

```
src/main/java/com/community/platform/
├── shared/                    # 공통 도메인
│   ├── domain/               # 도메인 이벤트, 기본 엔티티
│   └── infrastructure/       # 공통 인프라
├── user/                     # 사용자 관리 컨텍스트
│   ├── domain/              # User, UserProfile, 도메인 이벤트
│   ├── application/         # 서비스, DTO
│   └── infrastructure/      # 리포지토리, 외부 API
├── content/                 # 컨텐츠 관리 컨텍스트
│   ├── domain/             # Post, Comment, Category, Tag
│   ├── application/        # 서비스, DTO
│   └── infrastructure/     # 리포지토리
├── engagement/             # 참여/상호작용 컨텍스트
│   ├── domain/            # PostLike, PostScrap, PostView
│   ├── application/       # 서비스, DTO
│   └── infrastructure/    # 리포지토리
├── reward/                # 보상 시스템 컨텍스트
│   ├── domain/           # UserPoint, Badge, UserLevel
│   ├── application/      # 서비스, DTO
│   └── infrastructure/   # 리포지토리
├── notification/         # 알림 컨텍스트
│   ├── domain/          # Notification, NotificationTemplate
│   ├── application/     # 서비스, DTO
│   └── infrastructure/  # 리포지토리, 외부 알림 서비스
└── authorization/       # 인가 컨텍스트
    ├── domain/         # Menu, Permission, Role
    ├── application/    # 서비스, DTO
    └── infrastructure/ # 리포지토리
```

## 📊 데이터베이스 설계

### 핵심 테이블

- **users**: 사용자 기본 정보
- **user_profiles**: 사용자 프로필 정보
- **posts**: 게시글
- **comments**: 댓글 (대댓글 지원)
- **categories**: 계층형 카테고리
- **tags**: 태그 시스템
- **post_likes**: 게시글 좋아요
- **post_scraps**: 게시글 스크랩
- **notifications**: 알림

### 성능 최적화

- **인덱스**: 주요 검색 패턴에 맞는 복합 인덱스
- **캐시 컬럼**: 좋아요수, 댓글수 등 비정규화
- **파티셔닝**: 시간 기반 테이블 분할
- **읽기 복제본**: 조회 성능 향상

## 🚀 시작하기

### 필수 요구사항

- Java 17+
- Spring Boot 3.2+
- H2 Database (개발용) / PostgreSQL (운영용)
- Redis (캐시용)

### 프로젝트 실행

```bash
# 프로젝트 클론
cd DDD2

# 프로젝트 빌드
./gradlew clean build

# 애플리케이션 실행
./gradlew bootRun
```

### 접속 정보

- **애플리케이션**: http://localhost:8080/api
- **H2 Console**: http://localhost:8080/api/h2-console
- **Health Check**: http://localhost:8080/api/actuator/health

## 🔧 주요 기능

### 사용자 관리
- 회원가입/로그인
- 프로필 관리
- 팔로우/팔로잉
- 사용자 상태 관리 (활성/비활성/차단)

### 컨텐츠 관리
- 게시글 CRUD
- 댓글/대댓글 시스템
- 카테고리별 분류
- 태그 시스템
- 공지사항 기능

### 참여/상호작용
- 좋아요 시스템
- 스크랩 기능
- 조회수 추적
- 인기글/트렌딩

### 보상 시스템
- 포인트 적립/사용
- 레벨 시스템
- 뱃지 획득
- 활동 이력 추적

### 알림 시스템
- 실시간 알림
- 알림 설정 관리
- 알림 템플릿
- 다양한 알림 채널 지원

## 🏛️ DDD 원칙 적용

### 도메인 모델
- **애그리거트**: 일관성 경계 정의
- **엔티티**: 고유 식별자를 가진 도메인 객체
- **값 객체**: 불변 속성 그룹
- **도메인 이벤트**: 도메인 내 중요한 비즈니스 이벤트

### 비즈니스 규칙
- 도메인 엔티티 내 비즈니스 로직 캡슐화
- 불변식(Invariant) 보장
- 풍부한 도메인 모델(Rich Domain Model)

### 헥사고날 아키텍처
- **도메인**: 비즈니스 로직의 핵심
- **애플리케이션**: 유스케이스 조정
- **인프라**: 기술적 관심사 분리

## 📈 프로덕션 고려사항

### 성능
- 읽기/쓰기 분리 (CQRS)
- 캐시 전략
- 비동기 처리
- 배치 처리

### 확장성
- 마이크로서비스 전환 가능
- 이벤트 기반 아키텍처
- 수평 확장 지원

### 보안
- 역할 기반 접근 제어 (RBAC)
- 입력 검증
- SQL 인젝션 방지
- XSS 방지

### 모니터링
- 애플리케이션 메트릭
- 비즈니스 메트릭
- 성능 모니터링
- 로그 집계

## 📝 다음 단계

1. **리포지토리 계층**: Spring Data JPA 리포지토리 구현
2. **서비스 계층**: 애플리케이션 서비스 및 도메인 서비스
3. **API 계층**: REST API 컨트롤러 구현
4. **보안**: Spring Security 통합
5. **테스트**: 단위/통합/E2E 테스트
6. **캐시**: Redis 캐시 전략
7. **이벤트**: 도메인 이벤트 처리
8. **API 문서**: OpenAPI 3.0 문서화

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch
3. Commit your Changes
4. Push to the Branch
5. Open a Pull Request

## 📄 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다.