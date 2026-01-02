package com.community.platform.moderation.dto;

import com.community.platform.moderation.domain.PenaltyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 제재 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PenaltyCreateRequest {

    @NotNull(message = "대상 사용자 ID는 필수입니다")
    private Long targetUserId;

    @NotNull(message = "제재 타입은 필수입니다")
    private PenaltyType penaltyType;

    @NotBlank(message = "제재 사유는 필수입니다")
    private String reason;
}
