package com.community.platform.moderation.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 신고 검토 시작 이벤트
 */
@Getter
public class ReportReviewStartedEvent implements DomainEvent {
    private final Long reportId;
    private final Long reviewerId;
    private final LocalDateTime occurredOn;

    public ReportReviewStartedEvent(Long reportId, Long reviewerId) {
        this.reportId = reportId;
        this.reviewerId = reviewerId;
        this.occurredOn = LocalDateTime.now();
    }
}
