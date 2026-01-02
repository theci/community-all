package com.community.platform.moderation.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 신고 생성 이벤트
 */
@Getter
public class ReportCreatedEvent implements DomainEvent {
    private final Long reportId;
    private final Long reporterId;
    private final ReportTargetType targetType;
    private final Long targetId;
    private final ReportReason reason;
    private final LocalDateTime occurredOn;

    public ReportCreatedEvent(Long reportId, Long reporterId, ReportTargetType targetType,
                             Long targetId, ReportReason reason) {
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.occurredOn = LocalDateTime.now();
    }
}
