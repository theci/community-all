# DDD3 프로젝트 새 기능 구현 가이드

## 📋 필수 체크리스트

새로운 기능을 구현할 때 **반드시** 확인해야 할 항목들입니다.

---

## 1️⃣ 백엔드 (Spring Boot + DDD) 체크리스트

### ✅ 도메인 이벤트 구현 시

**❌ 잘못된 예시:**
```java
public class MessageCreatedEvent extends DomainEvent {
    // ...
}
```

**✅ 올바른 예시:**
```java
import java.time.LocalDateTime;

public class MessageCreatedEvent implements DomainEvent {  // implements 사용!
    private final LocalDateTime occurredOn;  // 필수 필드

    public MessageCreatedEvent(...) {
        this.occurredOn = LocalDateTime.now();  // 생성 시간 설정
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
```

**체크포인트:**
- [ ] `DomainEvent`는 **interface**이므로 `implements` 사용
- [ ] `occurredOn` 필드 필수 구현
- [ ] 생성자에서 `LocalDateTime.now()` 설정

---

### ✅ 예외 클래스 구현 시

**❌ 잘못된 예시:**
```java
import com.community.platform.shared.exception.BusinessException;  // 잘못된 패키지
```

**✅ 올바른 예시:**
```java
import com.community.platform.shared.domain.BusinessException;  // 올바른 패키지

public class MessageNotFoundException extends BusinessException {
    public MessageNotFoundException(Long messageId) {
        super("MESSAGE_NOT_FOUND", "메시지를 찾을 수 없습니다. ID: " + messageId);
    }
}
```

**체크포인트:**
- [ ] `BusinessException` 경로: `shared.domain.BusinessException` (NOT `shared.exception`)
- [ ] 첫 번째 인자: 에러 코드 (대문자_스네이크_케이스)
- [ ] 두 번째 인자: 사용자 친화적 메시지

---

### ✅ MapStruct 매퍼 구현 시

**❌ 잘못된 예시:**
```java
@Mapping(source = "profile.nickname", target = "nickname")  // User 엔티티 구조 확인 안함
UserSummaryResponse toUserSummaryResponse(User user);
```

**✅ 올바른 예시:**
```java
// 먼저 User 엔티티 구조 확인!
@Mapping(source = "nickname", target = "nickname")  // User에 직접 있음
@Mapping(target = "profileImageUrl", ignore = true)  // UserProfile에 있음
UserSummaryResponse toUserSummaryResponse(User user);

// 또는 default 메서드로 수동 매핑
default UserSummaryResponse toUserSummaryResponseWithProfile(User user, UserProfile profile) {
    return UserSummaryResponse.builder()
            .id(user.getId())
            .nickname(user.getNickname())
            .profileImageUrl(profile != null ? profile.getProfileImageUrl() : null)
            .build();
}
```

**체크포인트:**
- [ ] 대상 엔티티 구조를 **먼저 확인** (User.java, UserProfile.java 등)
- [ ] nested path 사용 전에 실제 필드 위치 확인
- [ ] 복잡한 매핑은 `default` 메서드 사용

---

### ✅ PageResponse 사용 시

**❌ 잘못된 예시:**
```java
return ApiResponse.success(PageResponse.from(messages));  // from() 메서드 없음
```

**✅ 올바른 예시:**
```java
return ApiResponse.success(PageResponse.of(messages));  // of() 메서드 사용
```

**체크포인트:**
- [ ] `PageResponse.of(Page<T>)` 메서드 사용 (NOT `from()`)

---

### ✅ Enum에 새 값 추가 시

**반드시 확인해야 할 파일들:**

#### 1. NotificationType 추가 시
```java
// /notification/domain/NotificationType.java
MESSAGE_RECEIVED("새 쪽지", "새로운 쪽지가 도착했습니다"),  // 새 타입 추가
```

**그리고 반드시:**
```java
// /notification/domain/NotificationPreference.java
public boolean isEnabled(NotificationType type) {
    return switch (type) {
        case MESSAGE_RECEIVED -> true;  // ⚠️ 새 케이스 추가 필수!
        // ... 기존 케이스들
    };
}
```

#### 2. RelatedEntityType 추가 시
```java
// /notification/domain/RelatedEntityType.java
public enum RelatedEntityType {
    POST,
    COMMENT,
    MESSAGE,  // ⚠️ 새 타입 추가
    // ...
}
```

**체크포인트:**
- [ ] Enum에 값 추가
- [ ] 해당 Enum을 사용하는 **모든 switch 문** 확인
- [ ] 컴파일 에러: `switch expression does not cover all possible input values` 주의

---

## 2️⃣ 프론트엔드 (Turborepo + Next.js) 체크리스트

### ✅ 새 타입 추가 시 (필수 4곳!)

새로운 타입을 추가할 때 **반드시 4곳 모두** 업데이트해야 합니다:

```
1. packages/types/src/message.ts         ← 공통 타입 정의
2. apps/web/lib/types/message.ts         ← Web 앱
3. apps/mobile-app/lib/types/message.ts  ← Mobile 앱
4. apps/admin/lib/types/message.ts       ← Admin 앱
```

**각 위치의 index.ts도 업데이트:**
```typescript
// packages/types/src/index.ts
export * from './message';  // ⚠️ export 추가

// apps/web/lib/types/index.ts
export * from './message';  // ⚠️ export 추가

// apps/mobile-app/lib/types/index.ts
export * from './message';  // ⚠️ export 추가

// apps/admin/lib/types/index.ts
export * from './message';  // ⚠️ export 추가
```

---

### ✅ 의존 타입 추가 시

**예시: Message 타입이 UserSummary를 사용하는 경우**

```typescript
// message.ts
import { UserSummary } from './user';  // ← 의존성

export interface Message {
  sender: UserSummary;
  recipient: UserSummary;
  // ...
}
```

**반드시 UserSummary도 정의:**

```
1. packages/types/src/user.ts         ← UserSummary 추가
2. apps/web/lib/types/user.ts         ← UserSummary 추가
3. apps/mobile-app/lib/types/user.ts  ← UserSummary 추가
4. apps/admin/lib/types/user.ts       ← UserSummary 추가
```

**체크포인트:**
- [ ] 새 타입 파일 생성 (4곳)
- [ ] index.ts에 export 추가 (4곳)
- [ ] 의존하는 타입도 모두 정의되어 있는지 확인
- [ ] import 경로가 올바른지 확인 (`'./user'` vs `'@ddd3/types'`)

---

### ✅ 새 서비스 추가 시

```
1. packages/types/src/message.ts              ← 타입 정의
2. apps/web/lib/services/messageService.ts    ← Web 서비스
3. apps/mobile-app/lib/services/messageService.ts
4. apps/admin/lib/services/messageService.ts

각 index.ts에도 export 추가!
```

---

## 3️⃣ 도커 빌드 전 최종 체크리스트

### ✅ 백엔드 빌드 전

```bash
# 1. 컴파일 에러 확인
cd backend
./gradlew compileJava

# 2. 주요 확인 사항
```
- [ ] DomainEvent는 `implements` 사용
- [ ] BusinessException 경로: `shared.domain`
- [ ] MapStruct 매핑이 실제 엔티티 구조와 일치
- [ ] PageResponse.of() 사용
- [ ] Enum 추가 시 모든 switch 문 업데이트

### ✅ 프론트엔드 빌드 전

```bash
# 타입 체크
cd apps/web
pnpm type-check
```

- [ ] packages/types에 타입 추가 및 export
- [ ] 3개 앱 모두 타입 추가 및 export
- [ ] 의존 타입 (UserSummary 등) 모두 정의
- [ ] import 경로 올바른지 확인

---

## 4️⃣ 새 기능 구현 플로우

### 1단계: 백엔드 구현
```
1. Domain 엔티티 작성
   - DomainEvent는 implements 사용
   - AggregateRoot 상속 시 이벤트 발행

2. Repository 작성
   - JpaRepository 상속
   - Custom query 메서드

3. DTO 작성
   - Request/Response 분리
   - Validation 어노테이션

4. Service 작성
   - @Transactional 적절히 사용
   - Exception은 shared.domain.BusinessException

5. Mapper 작성
   - 엔티티 구조 먼저 확인!
   - MapStruct @Mapper

6. Controller 작성
   - PageResponse.of() 사용

7. EventHandler 작성 (필요시)
   - @TransactionalEventListener

8. Enum 업데이트
   - NotificationType + NotificationPreference
   - RelatedEntityType
```

### 2단계: 프론트엔드 구현
```
1. packages/types/src에 타입 정의
   - 새 파일 생성
   - index.ts에 export 추가

2. 의존 타입 확인
   - UserSummary 등 필요한 타입 추가

3. 3개 앱에 타입 복사
   - apps/web/lib/types
   - apps/mobile-app/lib/types
   - apps/admin/lib/types
   - 각 index.ts에 export 추가

4. 서비스 구현
   - 동일하게 3개 앱 모두
   - API 엔드포인트 확인

5. 각 index.ts에 export 추가
```

### 3단계: 빌드 전 체크
```bash
# 백엔드
cd backend && ./gradlew compileJava

# 프론트엔드
cd apps/web && pnpm type-check
cd apps/mobile-app && pnpm type-check
cd apps/admin && pnpm type-check
```

### 4단계: 도커 빌드
```bash
docker-compose build backend
docker-compose build web
docker-compose build mobile-app
docker-compose build admin
```

---

## 🚨 자주 발생하는 에러와 해결

### 에러 1: `error: no interface expected here`
```
public class MessageCreatedEvent extends DomainEvent {
                                         ^
```
**해결:** `extends` → `implements` 변경

---

### 에러 2: `package shared.exception does not exist`
```
import com.community.platform.shared.exception.BusinessException;
```
**해결:** `shared.exception` → `shared.domain`

---

### 에러 3: `No property named "profile.nickname"`
```
@Mapping(source = "profile.nickname", target = "nickname")
```
**해결:** 엔티티 구조 먼저 확인 → `source = "nickname"`

---

### 에러 4: `cannot find symbol: method from(Page)`
```
PageResponse.from(messages)
```
**해결:** `from()` → `of()`

---

### 에러 5: `switch expression does not cover all possible input values`
```
return switch (type) {
    case COMMENT_ON_POST -> ...;
    // MESSAGE_RECEIVED 케이스 누락!
};
```
**해결:** Enum에 값 추가했으면 모든 switch 문 업데이트

---

### 에러 6: `Module has no exported member 'Message'`
```
import { Message } from '@ddd3/types';
```
**해결:**
1. packages/types/src/message.ts 생성
2. packages/types/src/index.ts에 export 추가
3. 빌드 캐시 클리어: `docker build --no-cache`

---

### 에러 7: `Module has no exported member 'UserSummary'`
```
import { UserSummary } from './user';
```
**해결:** **4곳 모두**에 UserSummary 인터페이스 추가
- packages/types/src/user.ts
- apps/web/lib/types/user.ts
- apps/mobile-app/lib/types/user.ts
- apps/admin/lib/types/user.ts

---

## 📝 구현 전 체크리스트 템플릿

새 기능 구현 시 이 체크리스트를 복사해서 사용하세요:

```markdown
## [기능명] 구현 체크리스트

### 백엔드
- [ ] Domain 엔티티 (DomainEvent는 implements)
- [ ] Repository
- [ ] DTO (Request/Response)
- [ ] Exception (shared.domain.BusinessException)
- [ ] Service (@Transactional)
- [ ] Mapper (엔티티 구조 확인)
- [ ] Controller (PageResponse.of())
- [ ] EventHandler
- [ ] NotificationType + NotificationPreference 업데이트
- [ ] RelatedEntityType 업데이트

### 프론트엔드
- [ ] packages/types/src/xxx.ts 생성
- [ ] packages/types/src/index.ts export 추가
- [ ] 의존 타입 추가 (UserSummary 등)
- [ ] apps/web/lib/types/xxx.ts
- [ ] apps/web/lib/types/index.ts export
- [ ] apps/web/lib/services/xxxService.ts
- [ ] apps/web/lib/services/index.ts export
- [ ] apps/mobile-app (동일)
- [ ] apps/admin (동일)
- [ ] pnpm type-check 통과 (3개 앱)
- [ ] page.tsx UI 생성

### 빌드
- [ ] docker-compose build backend
- [ ] docker-compose build web
- [ ] docker-compose build mobile-app
- [ ] docker-compose build admin
```

---

## 💡 팁

1. **엔티티 구조를 먼저 읽어라**: MapStruct 매핑 전에 항상 엔티티 파일을 먼저 확인
2. **4곳 동기화**: 타입 추가 시 packages + 3개 앱 모두 업데이트
3. **Enum은 switch 세트**: Enum 값 추가 = 모든 switch 문 업데이트
4. **빌드 캐시 주의**: 타입 변경 후에는 `--no-cache` 사용
5. **에러 로그 끝까지 읽기**: "Type error" 메시지에 정확한 위치와 원인 나옴

---

**마지막 업데이트**: 2026-01-07
