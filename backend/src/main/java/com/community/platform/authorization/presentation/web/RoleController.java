package com.community.platform.authorization.presentation.web;

import com.community.platform.authorization.application.RoleService;
import com.community.platform.authorization.domain.Permission;
import com.community.platform.authorization.domain.UserRole;
import com.community.platform.authorization.dto.*;
import com.community.platform.shared.dto.ApiResponse;
import com.community.platform.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 역할 및 권한 관리 REST API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 모든 역할 정보 조회
     * GET /api/v1/roles
     */
    @GetMapping
    public ApiResponse<List<RoleInfoResponse>> getAllRoles() {
        log.debug("모든 역할 정보 조회");

        List<RoleInfoResponse> roles = Arrays.stream(UserRole.values())
            .map(role -> RoleInfoResponse.builder()
                .role(role)
                .displayName(role.getDisplayName())
                .description(role.getDescription())
                .minimumLevel(role.getMinimumLevel())
                .permissions(role.getPermissions())
                .userCount(roleService.countUsersByRole(role))
                .build())
            .collect(Collectors.toList());

        return ApiResponse.success(roles);
    }

    /**
     * 특정 역할 정보 조회
     * GET /api/v1/roles/{role}
     */
    @GetMapping("/{role}")
    public ApiResponse<RoleInfoResponse> getRoleInfo(@PathVariable UserRole role) {
        log.debug("역할 정보 조회: {}", role);

        RoleInfoResponse response = RoleInfoResponse.builder()
            .role(role)
            .displayName(role.getDisplayName())
            .description(role.getDescription())
            .minimumLevel(role.getMinimumLevel())
            .permissions(role.getPermissions())
            .userCount(roleService.countUsersByRole(role))
            .build();

        return ApiResponse.success(response);
    }

    /**
     * 모든 권한 정보 조회
     * GET /api/v1/roles/permissions
     */
    @GetMapping("/permissions")
    public ApiResponse<List<Map<String, String>>> getAllPermissions() {
        log.debug("모든 권한 정보 조회");

        List<Map<String, String>> permissions = Arrays.stream(Permission.values())
            .map(permission -> {
                Map<String, String> info = new HashMap<>();
                info.put("name", permission.name());
                info.put("displayName", permission.getDisplayName());
                info.put("description", permission.getDescription());
                return info;
            })
            .collect(Collectors.toList());

        return ApiResponse.success(permissions);
    }

    /**
     * 특정 역할의 권한 목록 조회
     * GET /api/v1/roles/{role}/permissions
     */
    @GetMapping("/{role}/permissions")
    public ApiResponse<Permission[]> getRolePermissions(@PathVariable UserRole role) {
        log.debug("역할 권한 목록 조회: {}", role);

        Permission[] permissions = roleService.getRolePermissions(role);
        return ApiResponse.success(permissions);
    }

    /**
     * 사용자 권한 확인
     * GET /api/v1/roles/check-permission
     */
    @GetMapping("/check-permission")
    public ApiResponse<PermissionCheckResponse> checkPermission(
            @RequestParam Long userId,
            @RequestParam Permission permission) {
        log.debug("사용자 권한 확인: userId={}, permission={}", userId, permission);

        boolean hasPermission = roleService.hasPermission(userId, permission);

        PermissionCheckResponse response = PermissionCheckResponse.builder()
            .userId(userId)
            .permission(permission)
            .hasPermission(hasPermission)
            .message(hasPermission ? "권한이 있습니다" : "권한이 없습니다")
            .build();

        return ApiResponse.success(response);
    }

    /**
     * 역할별 사용자 통계
     * GET /api/v1/roles/statistics
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Long>> getRoleStatistics() {
        log.debug("역할별 사용자 통계 조회");

        Map<String, Long> statistics = new HashMap<>();
        for (UserRole role : UserRole.values()) {
            long count = roleService.countUsersByRole(role);
            statistics.put(role.name(), count);
        }

        return ApiResponse.success(statistics);
    }

    // ========== Admin APIs (관리자 전용) ==========

    /**
     * 사용자 역할 변경 (관리자 전용)
     * POST /api/v1/admin/roles/change
     */
    @PostMapping("/admin/change")
    public ApiResponse<Void> changeUserRole(
            @RequestParam Long currentUserId, // 관리자 ID
            @Valid @RequestBody RoleChangeRequest request) {
        log.info("사용자 역할 변경: adminId={}, targetUserId={}, newRole={}",
                currentUserId, request.getTargetUserId(), request.getNewRole());

        roleService.changeUserRole(request.getTargetUserId(), request.getNewRole(), currentUserId);

        return ApiResponse.success("역할이 변경되었습니다");
    }

    /**
     * 역할별 사용자 목록 조회 (관리자 전용)
     * GET /api/v1/admin/roles/{role}/users
     */
    @GetMapping("/admin/{role}/users")
    public ApiResponse<List<Map<String, Object>>> getUsersByRole(@PathVariable UserRole role) {
        log.debug("역할별 사용자 목록 조회: {}", role);

        List<User> users = roleService.getUsersByRole(role);
        List<Map<String, Object>> userInfos = users.stream()
            .map(user -> {
                Map<String, Object> info = new HashMap<>();
                info.put("id", user.getId());
                info.put("email", user.getEmail());
                info.put("nickname", user.getNickname());
                info.put("role", user.getRole());
                info.put("status", user.getStatus());
                info.put("createdAt", user.getCreatedAt());
                return info;
            })
            .collect(Collectors.toList());

        return ApiResponse.success(userInfos);
    }

    /**
     * 관리자 목록 조회 (관리자 전용)
     * GET /api/v1/admin/roles/admins
     */
    @GetMapping("/admin/admins")
    public ApiResponse<List<Map<String, Object>>> getAdminUsers() {
        log.debug("관리자 목록 조회");

        List<User> admins = roleService.getAdminUsers();
        List<Map<String, Object>> adminInfos = admins.stream()
            .map(user -> {
                Map<String, Object> info = new HashMap<>();
                info.put("id", user.getId());
                info.put("email", user.getEmail());
                info.put("nickname", user.getNickname());
                info.put("role", user.getRole());
                info.put("status", user.getStatus());
                info.put("createdAt", user.getCreatedAt());
                return info;
            })
            .collect(Collectors.toList());

        return ApiResponse.success(adminInfos);
    }
}
