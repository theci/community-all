package com.community.platform.user.domain;

import com.community.platform.authorization.domain.Permission;
import com.community.platform.authorization.domain.UserRole;
import com.community.platform.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
           @UniqueConstraint(name = "uk_user_nickname", columnNames = "nickname")
       },
       indexes = {
           @Index(name = "idx_user_email", columnList = "email"),
           @Index(name = "idx_user_nickname", columnList = "nickname"),
           @Index(name = "idx_user_status_created_at", columnList = "status, created_at"),
           @Index(name = "idx_user_role", columnList = "role")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends AggregateRoot {

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile profile;

    private User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.status = UserStatus.ACTIVE;
    }

    public static User register(String email, String encryptedPassword, String nickname) {
        validateEmail(email);
        validateNickname(nickname);
        return new User(email, encryptedPassword, nickname);
    }

    /**
     * 회원가입 완료 이벤트 발행 (저장 후 ID가 생성된 뒤 호출)
     */
    public void publishRegisteredEvent() {
        addDomainEvent(new UserRegisteredEvent(this.getId(), this.email, this.nickname));
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void block(String reason) {
        this.status = UserStatus.BLOCKED;
        addDomainEvent(new UserBlockedEvent(this.getId(), reason));
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
        addDomainEvent(new UserActivatedEvent(this.getId()));
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
        addDomainEvent(new UserDeactivatedEvent(this.getId()));
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isBlocked() {
        return this.status == UserStatus.BLOCKED;
    }

    /**
     * 역할 변경 (관리자만 가능)
     */
    public void changeRole(UserRole newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("역할은 필수입니다.");
        }
        this.role = newRole;
    }

    /**
     * 레벨 기반 자동 역할 승격
     */
    public void checkAndPromoteRole(int currentLevel) {
        if (UserRole.canAutoPromote(currentLevel, this.role)) {
            this.role = UserRole.getAutoPromotedRole(currentLevel, this.role);
        }
    }

    /**
     * 특정 권한 보유 여부 확인
     */
    public boolean hasPermission(Permission permission) {
        return this.role.hasPermission(permission);
    }

    /**
     * 여러 권한 중 하나라도 보유하는지 확인
     */
    public boolean hasAnyPermission(Permission... permissions) {
        return this.role.hasAnyPermission(permissions);
    }

    /**
     * 모든 권한을 보유하는지 확인
     */
    public boolean hasAllPermissions(Permission... permissions) {
        return this.role.hasAllPermissions(permissions);
    }

    /**
     * 관리자 여부 확인
     */
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN || this.role == UserRole.SUPER_ADMIN;
    }

    /**
     * 모더레이터 이상 여부 확인
     */
    public boolean isModerator() {
        return this.role == UserRole.MODERATOR || isAdmin();
    }

    private static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
    }

    private static void validateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }
        if (nickname.length() < 2 || nickname.length() > 50) {
            throw new IllegalArgumentException("닉네임은 2자 이상 50자 이하여야 합니다.");
        }
    }
}