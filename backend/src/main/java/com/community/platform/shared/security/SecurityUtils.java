package com.community.platform.shared.security;

import com.community.platform.shared.security.CustomUserDetailsService.CustomUserDetails;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * Spring Security 관련 유틸리티 클래스
 * 현재 인증된 사용자 정보를 쉽게 조회할 수 있는 정적 메서드들 제공
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityUtils {

    /**
     * 현재 인증된 사용자의 ID를 반환
     * @return 인증된 사용자 ID, 인증되지 않은 경우 Optional.empty()
     */
    public static Optional<Long> getCurrentUserId() {
        // CustomUserDetails에서 userId 추출 시도
        Optional<Long> userIdFromCustomDetails = getCurrentUserDetails()
                .map(CustomUserDetails::getUserId);

        if (userIdFromCustomDetails.isPresent()) {
            return userIdFromCustomDetails;
        }

        // JWT 인증의 경우 username에 userId가 문자열로 저장됨
        return getCurrentAuthentication()
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof UserDetails)
                .map(principal -> ((UserDetails) principal).getUsername())
                .flatMap(username -> {
                    try {
                        return Optional.of(Long.parseLong(username));
                    } catch (NumberFormatException e) {
                        return Optional.empty();
                    }
                });
    }

    /**
     * 현재 인증된 사용자의 이메일을 반환
     * @return 인증된 사용자 이메일, 인증되지 않은 경우 Optional.empty()
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUserDetails()
                .map(CustomUserDetails::getEmail);
    }

    /**
     * 현재 인증된 사용자의 닉네임을 반환
     * @return 인증된 사용자 닉네임, 인증되지 않은 경우 Optional.empty()
     */
    public static Optional<String> getCurrentUserNickname() {
        return getCurrentUserDetails()
                .map(CustomUserDetails::getNickname);
    }

    /**
     * 현재 인증된 사용자의 UserDetails를 반환
     * @return CustomUserDetails, 인증되지 않은 경우 Optional.empty()
     */
    public static Optional<CustomUserDetails> getCurrentUserDetails() {
        return getCurrentAuthentication()
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof CustomUserDetails)
                .map(principal -> (CustomUserDetails) principal);
    }

    /**
     * 현재 Authentication 객체를 반환
     * @return Authentication, 인증되지 않은 경우 Optional.empty()
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        
        return Optional.of(authentication);
    }

    /**
     * 현재 사용자가 인증되었는지 확인
     * @return 인증 여부
     */
    public static boolean isAuthenticated() {
        return getCurrentAuthentication().isPresent();
    }

    /**
     * 현재 사용자가 특정 권한을 가지고 있는지 확인
     * @param authority 확인할 권한 (예: "ROLE_ADMIN")
     * @return 권한 보유 여부
     */
    public static boolean hasAuthority(String authority) {
        return getCurrentAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals(authority)))
                .orElse(false);
    }

    /**
     * 현재 사용자가 관리자인지 확인
     * @return 관리자 여부
     */
    public static boolean isAdmin() {
        return hasAuthority("ROLE_ADMIN");
    }

    /**
     * 특정 사용자 ID가 현재 인증된 사용자와 같은지 확인
     * @param userId 비교할 사용자 ID
     * @return 동일 사용자 여부
     */
    public static boolean isCurrentUser(Long userId) {
        return getCurrentUserId()
                .map(currentUserId -> currentUserId.equals(userId))
                .orElse(false);
    }

    /**
     * 현재 사용자가 특정 리소스에 접근할 권한이 있는지 확인
     * (작성자이거나 관리자인 경우)
     * @param resourceOwnerId 리소스 소유자 ID
     * @return 접근 권한 여부
     */
    public static boolean canAccess(Long resourceOwnerId) {
        return isAdmin() || isCurrentUser(resourceOwnerId);
    }

    /**
     * 인증이 필요한 작업에서 현재 사용자 ID를 가져오거나 예외 발생
     * @return 현재 사용자 ID
     * @throws SecurityException 인증되지 않은 경우
     */
    public static Long requireCurrentUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new SecurityException("AUTH_REQUIRED", "인증이 필요합니다"));
    }

    /**
     * 인증이 필요한 작업에서 현재 사용자 정보를 가져오거나 예외 발생
     * @return CustomUserDetails
     * @throws SecurityException 인증되지 않은 경우
     */
    public static CustomUserDetails requireCurrentUserDetails() {
        return getCurrentUserDetails()
                .orElseThrow(() -> new SecurityException("AUTH_REQUIRED", "인증이 필요합니다"));
    }
}