package com.community.platform.notification.domain;

import com.community.platform.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 설정 엔티티
 * 사용자별 알림 수신 설정을 관리
 */
@Entity
@Table(name = "notification_preferences",
       indexes = {
           @Index(name = "uk_notification_pref_user", columnList = "user_id", unique = true)
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "notify_comment", nullable = false)
    private Boolean notifyComment = true;  // 내 글에 댓글

    @Column(name = "notify_reply", nullable = false)
    private Boolean notifyReply = true;  // 댓글에 답글

    @Column(name = "notify_like", nullable = false)
    private Boolean notifyLike = true;  // 좋아요

    @Column(name = "notify_scrap", nullable = false)
    private Boolean notifyScrap = true;  // 스크랩

    @Column(name = "notify_report_result", nullable = false)
    private Boolean notifyReportResult = true;  // 신고 결과

    @Column(name = "notify_level_up", nullable = false)
    private Boolean notifyLevelUp = true;  // 레벨업

    @Column(name = "notify_penalty", nullable = false)
    private Boolean notifyPenalty = true;  // 제재 통보

    private NotificationPreference(Long userId) {
        this.userId = userId;
        // 모든 알림 기본값 true (위 필드 초기화)
    }

    /**
     * 기본 알림 설정 생성 (모든 알림 활성화)
     */
    public static NotificationPreference createDefault(Long userId) {
        return new NotificationPreference(userId);
    }

    /**
     * 알림 설정 업데이트
     */
    public void updatePreferences(
            Boolean notifyComment,
            Boolean notifyReply,
            Boolean notifyLike,
            Boolean notifyScrap,
            Boolean notifyReportResult,
            Boolean notifyLevelUp,
            Boolean notifyPenalty
    ) {
        if (notifyComment != null) this.notifyComment = notifyComment;
        if (notifyReply != null) this.notifyReply = notifyReply;
        if (notifyLike != null) this.notifyLike = notifyLike;
        if (notifyScrap != null) this.notifyScrap = notifyScrap;
        if (notifyReportResult != null) this.notifyReportResult = notifyReportResult;
        if (notifyLevelUp != null) this.notifyLevelUp = notifyLevelUp;
        if (notifyPenalty != null) this.notifyPenalty = notifyPenalty;
    }

    /**
     * 특정 알림 타입에 대한 수신 여부 확인
     */
    public boolean isEnabled(NotificationType type) {
        return switch (type) {
            case COMMENT_ON_POST -> notifyComment;
            case REPLY_ON_COMMENT -> notifyReply;
            case LIKE_ON_POST -> notifyLike;
            case SCRAP_ON_POST -> notifyScrap;
            case MESSAGE_RECEIVED -> true;  // 쪽지는 항상 수신
            case REPORT_APPROVED, REPORT_REJECTED -> notifyReportResult;
            case LEVEL_UP -> notifyLevelUp;
            case PENALTY_APPLIED, PENALTY_EXPIRED -> notifyPenalty;
            case SYSTEM_NOTICE -> true;  // 시스템 공지는 항상 수신
        };
    }
}
