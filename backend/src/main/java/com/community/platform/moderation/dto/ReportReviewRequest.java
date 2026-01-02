package com.community.platform.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 신고 검토 요청 DTO (승인/반려)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportReviewRequest {

    @NotBlank(message = "검토 의견은 필수입니다")
    private String reviewComment;

    private String actionTaken; // 승인 시 취한 조치 (예: "게시글 삭제", "사용자 정지 3일")
}
