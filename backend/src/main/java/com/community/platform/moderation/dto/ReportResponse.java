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
 * 신고 응답 DTO (프론트엔드 호환)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {
    private Long id;
    private String reportType; // 프론트엔드 호환을 위해 reportType으로 변경
    private Long targetId;
    private String targetType;
    private String reason; // String으로 변경 (enum 이름)
    private String description;
    private String status; // String으로 변경 (enum 이름)
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String processingNote;

    // 사용자 정보
    private UserInfo reporter;
    private UserInfo reportedUser;
    private UserInfo processedBy;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
    }

    public static ReportResponse from(Report report, UserInfo reporter, UserInfo reportedUser, UserInfo processedBy) {
        return ReportResponse.builder()
            .id(report.getId())
            .reportType(report.getTargetType().name())
            .targetId(report.getTargetId())
            .targetType(report.getTargetType().name())
            .reason(report.getReason().name())
            .description(report.getDescription())
            .status(report.getStatus().name())
            .createdAt(report.getCreatedAt())
            .processedAt(report.getReviewedAt())
            .processingNote(report.getReviewComment())
            .reporter(reporter)
            .reportedUser(reportedUser)
            .processedBy(processedBy)
            .build();
    }
}
