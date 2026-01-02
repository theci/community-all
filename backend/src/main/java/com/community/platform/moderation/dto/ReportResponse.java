package com.community.platform.moderation.dto;

import com.community.platform.moderation.domain.Report;
import com.community.platform.moderation.domain.ReportReason;
import com.community.platform.moderation.domain.ReportStatus;
import com.community.platform.moderation.domain.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 신고 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {
    private Long id;
    private Long reporterId;
    private Long reportedUserId;
    private ReportTargetType targetType;
    private Long targetId;
    private ReportReason reason;
    private String description;
    private ReportStatus status;
    private Long reviewerId;
    private String reviewComment;
    private LocalDateTime reviewedAt;
    private String actionTaken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
            .id(report.getId())
            .reporterId(report.getReporterId())
            .reportedUserId(report.getReportedUserId())
            .targetType(report.getTargetType())
            .targetId(report.getTargetId())
            .reason(report.getReason())
            .description(report.getDescription())
            .status(report.getStatus())
            .reviewerId(report.getReviewerId())
            .reviewComment(report.getReviewComment())
            .reviewedAt(report.getReviewedAt())
            .actionTaken(report.getActionTaken())
            .createdAt(report.getCreatedAt())
            .updatedAt(report.getUpdatedAt())
            .build();
    }
}
