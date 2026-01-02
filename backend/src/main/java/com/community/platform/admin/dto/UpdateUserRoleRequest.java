package com.community.platform.admin.dto;

import com.community.platform.authorization.domain.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 역할 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class UpdateUserRoleRequest {

    @NotNull(message = "역할은 필수입니다")
    private UserRole role;
}
