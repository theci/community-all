package com.community.platform.moderation.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 신고 승인 이벤트
 * 신고가 승인되면 대상 콘텐츠 삭제, 사용자 제재 등의 조치가 필요할 수 있음
 */
@Getter
public class ReportApprovedEvent implements DomainEvent {
    private final Long reportId;
    private final Long reporterId;  // 신고자 ID
    private final Long reviewerId;
    private final ReportTargetType targetType;
    private final Long targetId;
    private final Long reportedUserId;
    private final ReportReason reason;
    private final String actionTaken;
    private final LocalDateTime occurredOn;

    public ReportApprovedEvent(Long reportId, Long reporterId, Long reviewerId,
                              ReportTargetType targetType, Long targetId,
                              Long reportedUserId, ReportReason reason, String actionTaken) {
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.reviewerId = reviewerId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reportedUserId = reportedUserId;
        this.reason = reason;
        this.actionTaken = actionTaken;
        this.occurredOn = LocalDateTime.now();
    }
}
