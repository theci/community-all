package com.community.platform.moderation.domain;

import com.community.platform.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 신고 엔티티
 * 게시글, 댓글, 채팅, 사용자에 대한 신고를 관리
 */
@Entity
@Table(name = "reports",
       indexes = {
           @Index(name = "idx_report_reporter_created", columnList = "reporter_id, created_at DESC"),
           @Index(name = "idx_report_target", columnList = "target_type, target_id, status"),
           @Index(name = "idx_report_status_created", columnList = "status, created_at DESC"),
           @Index(name = "idx_report_reported_user", columnList = "reported_user_id, status"),
           @Index(name = "idx_report_reviewer", columnList = "reviewer_id, reviewed_at")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends AggregateRoot {

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId; // 신고자 ID

    @Column(name = "reported_user_id")
    private Long reportedUserId; // 신고 대상 사용자 ID (게시글/댓글 작성자 또는 사용자 자체)

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTargetType targetType; // 신고 대상 유형 (POST, COMMENT, CHAT, USER)

    @Column(name = "target_id", nullable = false)
    private Long targetId; // 신고 대상 ID

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 30)
    private ReportReason reason; // 신고 사유

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 신고 상세 설명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING; // 신고 상태

    @Column(name = "reviewer_id")
    private Long reviewerId; // 신고 검토자 ID

    @Lob
    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment; // 검토 의견

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt; // 검토 완료 일시

    @Column(name = "action_taken", length = 100)
    private String actionTaken; // 취한 조치 (예: "게시글 삭제", "사용자 정지 3일")

    private Report(Long reporterId, Long reportedUserId, ReportTargetType targetType,
                   Long targetId, ReportReason reason, String description) {
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.description = description;
        this.status = ReportStatus.PENDING;

        addDomainEvent(new ReportCreatedEvent(this.getId(), reporterId, targetType, targetId, reason));
    }

    /**
     * 신고 생성
     */
    public static Report create(Long reporterId, Long reportedUserId, ReportTargetType targetType,
                                Long targetId, ReportReason reason, String description) {
        validateReporter(reporterId);
        validateTarget(targetType, targetId);
        validateReason(reason);

        return new Report(reporterId, reportedUserId, targetType, targetId, reason, description);
    }

    /**
     * 신고 검토 시작
     */
    public void startReview(Long reviewerId) {
        if (reviewerId == null) {
            throw new IllegalArgumentException("검토자 ID는 필수입니다.");
        }

        if (!status.canTransitionTo(ReportStatus.IN_REVIEW)) {
            throw new IllegalStateException(
                String.format("현재 상태(%s)에서 검토 시작할 수 없습니다.", status.getDisplayName())
            );
        }

        this.status = ReportStatus.IN_REVIEW;
        this.reviewerId = reviewerId;

        addDomainEvent(new ReportReviewStartedEvent(this.getId(), reviewerId));
    }

    /**
     * 신고 승인 (조치 완료)
     */
    public void approve(Long reviewerId, String reviewComment, String actionTaken) {
        validateReviewer(reviewerId);

        if (!status.canTransitionTo(ReportStatus.APPROVED)) {
            throw new IllegalStateException(
                String.format("현재 상태(%s)에서 승인할 수 없습니다.", status.getDisplayName())
            );
        }

        this.status = ReportStatus.APPROVED;
        this.reviewerId = reviewerId;
        this.reviewComment = reviewComment;
        this.actionTaken = actionTaken;
        this.reviewedAt = LocalDateTime.now();

        addDomainEvent(new ReportApprovedEvent(
            this.getId(), this.reporterId, reviewerId, this.targetType,
            this.targetId, this.reportedUserId, this.reason, actionTaken
        ));
    }

    /**
     * 신고 반려
     */
    public void reject(Long reviewerId, String reviewComment) {
        validateReviewer(reviewerId);

        if (!status.canTransitionTo(ReportStatus.REJECTED)) {
            throw new IllegalStateException(
                String.format("현재 상태(%s)에서 반려할 수 없습니다.", status.getDisplayName())
            );
        }

        this.status = ReportStatus.REJECTED;
        this.reviewerId = reviewerId;
        this.reviewComment = reviewComment;
        this.reviewedAt = LocalDateTime.now();

        addDomainEvent(new ReportRejectedEvent(this.getId(), this.reporterId, reviewerId, reviewComment));
    }

    /**
     * 신고가 완료되었는지 확인
     */
    public boolean isCompleted() {
        return status.isCompleted();
    }

    /**
     * 신고가 승인되었는지 확인
     */
    public boolean isApproved() {
        return status == ReportStatus.APPROVED;
    }

    /**
     * 신고가 반려되었는지 확인
     */
    public boolean isRejected() {
        return status == ReportStatus.REJECTED;
    }

    /**
     * 특정 사용자가 신고한 것인지 확인
     */
    public boolean isReportedBy(Long userId) {
        return this.reporterId.equals(userId);
    }

    /**
     * 고위험 신고인지 확인
     */
    public boolean isHighSeverity() {
        return reason.isHighSeverity();
    }

    // Validation methods
    private static void validateReporter(Long reporterId) {
        if (reporterId == null) {
            throw new IllegalArgumentException("신고자 ID는 필수입니다.");
        }
    }

    private static void validateTarget(ReportTargetType targetType, Long targetId) {
        if (targetType == null) {
            throw new IllegalArgumentException("신고 대상 유형은 필수입니다.");
        }
        if (targetId == null) {
            throw new IllegalArgumentException("신고 대상 ID는 필수입니다.");
        }
    }

    private static void validateReason(ReportReason reason) {
        if (reason == null) {
            throw new IllegalArgumentException("신고 사유는 필수입니다.");
        }
    }

    private void validateReviewer(Long reviewerId) {
        if (reviewerId == null) {
            throw new IllegalArgumentException("검토자 ID는 필수입니다.");
        }
    }
}
