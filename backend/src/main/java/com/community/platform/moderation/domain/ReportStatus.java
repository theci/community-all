package com.community.platform.moderation.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 신고 처리 상태
 */
@Getter
@RequiredArgsConstructor
public enum ReportStatus {
    PENDING("접수", "신고가 접수되어 검토 대기 중입니다"),
    IN_REVIEW("검토중", "관리자가 신고 내용을 검토하고 있습니다"),
    APPROVED("승인", "신고가 승인되어 조치가 완료되었습니다"),
    REJECTED("반려", "신고가 반려되었습니다");

    private final String displayName;
    private final String description;

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isInReview() {
        return this == IN_REVIEW;
    }

    public boolean isCompleted() {
        return this == APPROVED || this == REJECTED;
    }

    public boolean canTransitionTo(ReportStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == IN_REVIEW || newStatus == APPROVED || newStatus == REJECTED;
            case IN_REVIEW -> newStatus == APPROVED || newStatus == REJECTED;
            case APPROVED, REJECTED -> false;
        };
    }
}
