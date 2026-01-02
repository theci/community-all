package com.community.platform.moderation.domain;

import com.community.platform.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 사용자 제재 엔티티
 * 신고 승인, 관리자 수동 부여 등으로 사용자에게 제재를 부과
 */
@Entity
@Table(name = "user_penalties",
       indexes = {
           @Index(name = "idx_penalty_user_active", columnList = "user_id, is_active, end_date"),
           @Index(name = "idx_penalty_active_expired", columnList = "is_active, end_date")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPenalty extends AggregateRoot {

    @Column(name = "user_id", nullable = false)
    private Long userId;  // 제재 대상 사용자 ID

    @Enumerated(EnumType.STRING)
    @Column(name = "penalty_type", nullable = false, length = 30)
    private PenaltyType penaltyType;  // 제재 종류

    @Column(length = 500)
    private String reason;  // 제재 사유

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;  // 제재 시작일

    @Column(name = "end_date")
    private LocalDateTime endDate;  // 제재 종료일 (null = 영구)

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;  // 활성 상태

    @Column(name = "created_by")
    private Long createdBy;  // 제재 부여자 (관리자 ID, null = 자동)

    private UserPenalty(Long userId, PenaltyType penaltyType, String reason,
                        LocalDateTime startDate, LocalDateTime endDate, Long createdBy) {
        this.userId = userId;
        this.penaltyType = penaltyType;
        this.reason = reason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = true;
        this.createdBy = createdBy;

        addDomainEvent(new UserPenaltyCreatedEvent(
                this.getId(), userId, penaltyType, reason, startDate, endDate
        ));
    }

    /**
     * 제재 생성 (기간 지정)
     */
    public static UserPenalty create(Long userId, PenaltyType penaltyType, String reason,
                                     Duration duration, Long createdBy) {
        validateUserId(userId);
        validatePenaltyType(penaltyType);
        validateReason(reason);

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = duration != null ? startDate.plus(duration) : null;

        return new UserPenalty(userId, penaltyType, reason, startDate, endDate, createdBy);
    }

    /**
     * 제재 생성 (PenaltyType의 기본 기간 사용)
     */
    public static UserPenalty create(Long userId, PenaltyType penaltyType, String reason, Long createdBy) {
        return create(userId, penaltyType, reason, penaltyType.getDuration(), createdBy);
    }

    /**
     * 제재가 현재 활성화되어 있는지 확인
     */
    public boolean isActive() {
        if (!this.isActive) {
            return false;
        }

        // 만료일 체크
        if (endDate != null && LocalDateTime.now().isAfter(endDate)) {
            return false;
        }

        return true;
    }

    /**
     * 제재 만료 (수동)
     */
    public void expire() {
        if (!this.isActive) {
            throw new IllegalStateException("이미 만료된 제재입니다.");
        }

        this.isActive = false;
        addDomainEvent(new UserPenaltyExpiredEvent(this.getId(), this.userId, this.penaltyType));
    }

    /**
     * 영구 제재 여부
     */
    public boolean isPermanent() {
        return endDate == null;
    }

    /**
     * 제재 종료까지 남은 시간 (밀리초)
     */
    public Long getRemainingMillis() {
        if (isPermanent()) {
            return null;
        }

        if (endDate == null) {
            return null;
        }

        long remaining = Duration.between(LocalDateTime.now(), endDate).toMillis();
        return remaining > 0 ? remaining : 0L;
    }

    // Validation methods
    private static void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
    }

    private static void validatePenaltyType(PenaltyType penaltyType) {
        if (penaltyType == null) {
            throw new IllegalArgumentException("제재 타입은 필수입니다.");
        }
    }

    private static void validateReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("제재 사유는 필수입니다.");
        }
        if (reason.length() > 500) {
            throw new IllegalArgumentException("제재 사유는 500자를 초과할 수 없습니다.");
        }
    }
}
