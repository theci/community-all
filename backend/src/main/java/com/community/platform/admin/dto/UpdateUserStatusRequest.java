package com.community.platform.admin.dto;

import com.community.platform.user.domain.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 상태 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class UpdateUserStatusRequest {

    @NotNull(message = "상태는 필수입니다")
    private UserStatus status;

    private String reason; // 차단 사유 등
}
