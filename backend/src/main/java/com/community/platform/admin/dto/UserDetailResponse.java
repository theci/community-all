package com.community.platform.admin.dto;

import com.community.platform.authorization.domain.UserRole;
import com.community.platform.user.domain.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 상세 정보 응답 DTO
 */
@Getter
@Builder
public class UserDetailResponse {

    private Long userId;
    private String email;
    private String nickname;
    private UserStatus status;
    private UserRole role;
    private Integer currentPoints;
    private Integer currentLevel;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    // 프로필 정보
    private String bio;
    private String avatarUrl;

    // 활동 통계
    private Long totalPosts;
    private Long totalComments;
    private Long totalLikes;
    private Long totalScraps;

    // 제재 이력
    private List<SanctionInfo> sanctions;

    @Getter
    @Builder
    public static class SanctionInfo {
        private Long sanctionId;
        private String type;
        private String reason;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String status;
    }
}
