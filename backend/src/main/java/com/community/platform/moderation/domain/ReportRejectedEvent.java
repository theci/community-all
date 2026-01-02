package com.community.platform.moderation.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 신고 반려 이벤트
 */
@Getter
public class ReportRejectedEvent implements DomainEvent {
    private final Long reportId;
    private final Long reporterId;  // 신고자 ID
    private final Long reviewerId;
    private final String reviewComment;
    private final LocalDateTime occurredOn;

    public ReportRejectedEvent(Long reportId, Long reporterId, Long reviewerId, String reviewComment) {
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.reviewerId = reviewerId;
        this.reviewComment = reviewComment;
        this.occurredOn = LocalDateTime.now();
    }

    // For notification handler compatibility
    public String getRejectReason() {
        return reviewComment;
    }
}
