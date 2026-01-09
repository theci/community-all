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
 * 신고 상세 응답 DTO (사용자 정보 및 콘텐츠 포함)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDetailResponse {
    private Long id;
    private String reportType; // 프론트엔드 호환을 위해 reportType으로 변경
    private Long targetId;
    private String targetType;
    private ReportReason reason;
    private String description;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String processingNote;

    // 신고자 정보
    private UserInfo reporter;

    // 신고된 사용자 정보
    private UserInfo reportedUser;

    // 처리자 정보
    private UserInfo processedBy;

    // 신고된 콘텐츠 정보
    private TargetContentInfo targetContent;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TargetContentInfo {
        private Long id;
        private String content;
        private UserInfo author;
    }

    public static ReportDetailResponse from(
            Report report,
            UserInfo reporter,
            UserInfo reportedUser,
            UserInfo processedBy,
            TargetContentInfo targetContent
    ) {
        return ReportDetailResponse.builder()
                .id(report.getId())
                .reportType(report.getTargetType().name())
                .targetId(report.getTargetId())
                .targetType(report.getTargetType().name())
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .processedAt(report.getReviewedAt())
                .processingNote(report.getReviewComment())
                .reporter(reporter)
                .reportedUser(reportedUser)
                .processedBy(processedBy)
                .targetContent(targetContent)
                .build();
    }
}
