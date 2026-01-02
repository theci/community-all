package com.community.platform.reward.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자 포인트 조정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class AdminPointAdjustRequest {

    @NotNull(message = "대상 사용자 ID는 필수입니다")
    private Long targetUserId;

    @NotNull(message = "포인트는 필수입니다")
    private Integer points; // 양수: 지급, 음수: 차감

    @NotBlank(message = "사유는 필수입니다")
    private String reason;
}
