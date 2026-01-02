package com.community.platform.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 포인트 조정 요청 DTO
 */
@Getter
@NoArgsConstructor
public class AdjustUserPointRequest {

    @NotNull(message = "조정할 포인트는 필수입니다")
    private Integer points;

    @NotNull(message = "사유는 필수입니다")
    private String reason;
}
