package com.community.platform.authorization.dto;

import com.community.platform.authorization.domain.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 역할 변경 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class RoleChangeRequest {

    @NotNull(message = "대상 사용자 ID는 필수입니다")
    private Long targetUserId;

    @NotNull(message = "새로운 역할은 필수입니다")
    private UserRole newRole;

    private String reason; // 변경 사유
}
