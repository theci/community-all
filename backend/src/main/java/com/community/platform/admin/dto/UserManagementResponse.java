package com.community.platform.admin.dto;

import com.community.platform.authorization.domain.UserRole;
import com.community.platform.user.domain.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 관리 목록 응답 DTO
 */
@Getter
@Builder
public class UserManagementResponse {

    private Long userId;
    private String email;
    private String nickname;
    private UserStatus status;
    private UserRole role;
    private Integer currentPoints;
    private Integer currentLevel;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private Long totalPosts;
    private Long totalComments;
}
