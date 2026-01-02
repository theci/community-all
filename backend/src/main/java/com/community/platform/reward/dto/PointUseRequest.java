package com.community.platform.reward.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 포인트 사용 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class PointUseRequest {

    @NotNull(message = "사용할 포인트는 필수입니다")
    @Min(value = 1, message = "사용할 포인트는 1 이상이어야 합니다")
    private Integer points;

    @NotBlank(message = "사용 내역은 필수입니다")
    private String description;
}
