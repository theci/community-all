package com.community.platform.moderation.dto;

import com.community.platform.moderation.domain.ReportReason;
import com.community.platform.moderation.domain.ReportTargetType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 신고 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreateRequest {

    @NotNull(message = "신고 대상 유형은 필수입니다")
    private ReportTargetType targetType;

    @NotNull(message = "신고 대상 ID는 필수입니다")
    private Long targetId;

    @NotNull(message = "신고 사유는 필수입니다")
    private ReportReason reason;

    private String description; // 선택적 상세 설명
}
