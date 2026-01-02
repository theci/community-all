package com.community.platform.authorization.application;

import com.community.platform.authorization.domain.Permission;
import com.community.platform.authorization.domain.UserRole;
import com.community.platform.user.domain.User;
import com.community.platform.user.exception.UserNotFoundException;
import com.community.platform.user.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 역할 및 권한 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {

    private final UserRepository userRepository;

    /**
     * 사용자 역할 변경 (관리자만 가능)
     */
    @Transactional
    public void changeUserRole(Long userId, UserRole newRole, Long adminId) {
        log.info("사용자 역할 변경. userId: {}, newRole: {}, adminId: {}", userId, newRole, adminId);

        // 관리자 권한 확인
        User admin = getUserById(adminId);
        if (!admin.hasPermission(Permission.ROLE_ASSIGN)) {
            throw new IllegalArgumentException("역할 변경 권한이 없습니다.");
        }

        // 대상 사용자 조회
        User user = getUserById(userId);

        // SUPER_ADMIN 역할은 변경 불가
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new IllegalStateException("최고 관리자 역할은 변경할 수 없습니다.");
        }

        // 자기 자신의 역할 변경 방지
        if (userId.equals(adminId)) {
            throw new IllegalStateException("자신의 역할은 변경할 수 없습니다.");
        }

        user.changeRole(newRole);
        userRepository.save(user);

        log.info("사용자 역할 변경 완료. userId: {}, newRole: {}", userId, newRole);
    }

    /**
     * 사용자 권한 확인
     */
    public boolean hasPermission(Long userId, Permission permission) {
        User user = getUserById(userId);
        return user.hasPermission(permission);
    }

    /**
     * 사용자가 여러 권한 중 하나라도 보유하는지 확인
     */
    public boolean hasAnyPermission(Long userId, Permission... permissions) {
        User user = getUserById(userId);
        return user.hasAnyPermission(permissions);
    }

    /**
     * 사용자가 모든 권한을 보유하는지 확인
     */
    public boolean hasAllPermissions(Long userId, Permission... permissions) {
        User user = getUserById(userId);
        return user.hasAllPermissions(permissions);
    }

    /**
     * 역할별 사용자 수 조회
     */
    public long countUsersByRole(UserRole role) {
        return userRepository.countByRole(role);
    }

    /**
     * 역할별 사용자 목록 조회
     */
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * 관리자 목록 조회 (MODERATOR, ADMIN, SUPER_ADMIN)
     */
    public List<User> getAdminUsers() {
        return userRepository.findByRoleIn(
            List.of(UserRole.MODERATOR, UserRole.ADMIN, UserRole.SUPER_ADMIN)
        );
    }

    /**
     * 레벨 기반 자동 역할 승격 체크
     */
    @Transactional
    public void checkAndPromoteRole(Long userId, int currentLevel) {
        log.debug("레벨 기반 역할 승격 체크. userId: {}, level: {}", userId, currentLevel);

        User user = getUserById(userId);
        UserRole beforeRole = user.getRole();

        user.checkAndPromoteRole(currentLevel);

        if (user.getRole() != beforeRole) {
            userRepository.save(user);
            log.info("레벨 기반 역할 자동 승격. userId: {}, {} -> {}",
                    userId, beforeRole, user.getRole());
        }
    }

    /**
     * 사용자 조회
     */
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * 모든 역할 정보 조회
     */
    public UserRole[] getAllRoles() {
        return UserRole.values();
    }

    /**
     * 모든 권한 정보 조회
     */
    public Permission[] getAllPermissions() {
        return Permission.values();
    }

    /**
     * 특정 역할의 권한 목록 조회
     */
    public Permission[] getRolePermissions(UserRole role) {
        return role.getPermissions();
    }
}
