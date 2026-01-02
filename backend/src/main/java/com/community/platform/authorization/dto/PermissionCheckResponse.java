package com.community.platform.authorization.dto;

import com.community.platform.authorization.domain.Permission;
import lombok.Builder;
import lombok.Getter;

/**
 * 권한 확인 응답 DTO
 */
@Getter
@Builder
public class PermissionCheckResponse {
    private Long userId;
    private Permission permission;
    private boolean hasPermission;
    private String message;
}
