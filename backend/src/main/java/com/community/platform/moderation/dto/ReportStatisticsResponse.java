package com.community.platform.moderation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 신고 통계 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportStatisticsResponse {
    private Long totalReports;      // 전체 신고 수
    private Long pendingReports;    // 대기중인 신고 수
    private Long inReviewReports;   // 검토중인 신고 수
    private Long approvedReports;   // 승인된 신고 수
    private Long rejectedReports;   // 반려된 신고 수
    private Long highSeverityReports; // 고위험 신고 수
}
