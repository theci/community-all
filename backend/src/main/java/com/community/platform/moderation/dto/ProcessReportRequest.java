package com.community.platform.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 신고 처리 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessReportRequest {
    @NotBlank(message = "처리 상태는 필수입니다")
    private String status; // APPROVED 또는 REJECTED

    private String processingNote; // 처리 메모

    private String actionTaken; // 조치 내용 (예: "콘텐츠 삭제", "경고", "무조치")

    private Boolean applyActions; // 조치 적용 여부
}
