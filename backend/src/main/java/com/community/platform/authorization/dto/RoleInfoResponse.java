package com.community.platform.authorization.dto;

import com.community.platform.authorization.domain.Permission;
import com.community.platform.authorization.domain.UserRole;
import lombok.Builder;
import lombok.Getter;

/**
 * 역할 정보 응답 DTO
 */
@Getter
@Builder
public class RoleInfoResponse {
    private UserRole role;
    private String displayName;
    private String description;
    private int minimumLevel;
    private Permission[] permissions;
    private long userCount; // 해당 역할의 사용자 수
}
