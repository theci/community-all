package com.community.platform.authorization.domain;

import lombok.Getter;

/**
 * 사용자 역할 (Role)
 * 역할 기반 접근 제어 (RBAC)
 */
@Getter
public enum UserRole {
    USER(
        "일반 유저",
        "기본 권한을 가진 일반 사용자",
        0,
        new Permission[]{
            Permission.POST_READ,
            Permission.POST_CREATE,
            Permission.POST_UPDATE_OWN,
            Permission.POST_DELETE_OWN,
            Permission.COMMENT_READ,
            Permission.COMMENT_CREATE,
            Permission.COMMENT_UPDATE_OWN,
            Permission.COMMENT_DELETE_OWN,
            Permission.LIKE_CREATE,
            Permission.SCRAP_CREATE
        }
    ),

    POWER_USER(
        "파워 유저",
        "높은 레벨의 활동적인 사용자 (레벨 7+)",
        7,
        new Permission[]{
            Permission.POST_READ,
            Permission.POST_CREATE,
            Permission.POST_UPDATE_OWN,
            Permission.POST_DELETE_OWN,
            Permission.POST_RECOMMEND,
            Permission.COMMENT_READ,
            Permission.COMMENT_CREATE,
            Permission.COMMENT_UPDATE_OWN,
            Permission.COMMENT_DELETE_OWN,
            Permission.LIKE_CREATE,
            Permission.SCRAP_CREATE,
            Permission.REPORT_CREATE,
            Permission.USER_TEMP_BLOCK
        }
    ),

    MODERATOR(
        "부관리자",
        "콘텐츠 관리 및 신고 처리 권한",
        0,
        new Permission[]{
            Permission.POST_READ,
            Permission.POST_CREATE,
            Permission.POST_UPDATE_OWN,
            Permission.POST_DELETE_OWN,
            Permission.POST_UPDATE_ALL,
            Permission.POST_DELETE_ALL,
            Permission.POST_PIN,
            Permission.COMMENT_READ,
            Permission.COMMENT_CREATE,
            Permission.COMMENT_UPDATE_OWN,
            Permission.COMMENT_DELETE_OWN,
            Permission.COMMENT_UPDATE_ALL,
            Permission.COMMENT_DELETE_ALL,
            Permission.LIKE_CREATE,
            Permission.SCRAP_CREATE,
            Permission.REPORT_CREATE,
            Permission.REPORT_MANAGE,
            Permission.USER_BLOCK,
            Permission.CONTENT_FILTER_VIEW
        }
    ),

    ADMIN(
        "운영자",
        "시스템 관리 및 사용자 관리 권한",
        0,
        new Permission[]{
            Permission.POST_READ,
            Permission.POST_CREATE,
            Permission.POST_UPDATE_OWN,
            Permission.POST_DELETE_OWN,
            Permission.POST_UPDATE_ALL,
            Permission.POST_DELETE_ALL,
            Permission.POST_PIN,
            Permission.COMMENT_READ,
            Permission.COMMENT_CREATE,
            Permission.COMMENT_UPDATE_OWN,
            Permission.COMMENT_DELETE_OWN,
            Permission.COMMENT_UPDATE_ALL,
            Permission.COMMENT_DELETE_ALL,
            Permission.LIKE_CREATE,
            Permission.SCRAP_CREATE,
            Permission.REPORT_CREATE,
            Permission.REPORT_MANAGE,
            Permission.USER_MANAGE,
            Permission.USER_BLOCK,
            Permission.ROLE_ASSIGN,
            Permission.POINT_MANAGE,
            Permission.CONTENT_FILTER_MANAGE,
            Permission.NOTICE_MANAGE,
            Permission.CATEGORY_MANAGE,
            Permission.STATISTICS_VIEW
        }
    ),

    SUPER_ADMIN(
        "최고 관리자",
        "모든 권한을 가진 최고 관리자",
        0,
        new Permission[]{
            Permission.ALL
        }
    );

    private final String displayName;
    private final String description;
    private final int minimumLevel; // 역할 획득에 필요한 최소 레벨 (0 = 레벨 무관)
    private final Permission[] permissions;

    UserRole(String displayName, String description, int minimumLevel, Permission[] permissions) {
        this.displayName = displayName;
        this.description = description;
        this.minimumLevel = minimumLevel;
        this.permissions = permissions;
    }

    /**
     * 특정 권한 보유 여부 확인
     */
    public boolean hasPermission(Permission permission) {
        // SUPER_ADMIN은 모든 권한 보유
        if (this == SUPER_ADMIN) {
            return true;
        }

        for (Permission p : permissions) {
            if (p == permission || p == Permission.ALL) {
                return true;
            }
        }
        return false;
    }

    /**
     * 여러 권한 중 하나라도 보유하는지 확인
     */
    public boolean hasAnyPermission(Permission... requiredPermissions) {
        for (Permission required : requiredPermissions) {
            if (hasPermission(required)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 모든 권한을 보유하는지 확인
     */
    public boolean hasAllPermissions(Permission... requiredPermissions) {
        for (Permission required : requiredPermissions) {
            if (!hasPermission(required)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 레벨 기반 역할 자동 승격 가능 여부
     */
    public static boolean canAutoPromote(int userLevel, UserRole currentRole) {
        // POWER_USER는 레벨 7 이상이면 자동 승격 가능
        return userLevel >= POWER_USER.minimumLevel && currentRole == USER;
    }

    /**
     * 레벨에 따른 자동 승격
     */
    public static UserRole getAutoPromotedRole(int userLevel, UserRole currentRole) {
        if (canAutoPromote(userLevel, currentRole)) {
            return POWER_USER;
        }
        return currentRole;
    }

    /**
     * Spring Security Authority 문자열 반환
     */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
