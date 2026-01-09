package com.community.platform.user.presentation.web;

import com.community.platform.shared.dto.ApiResponse;
import com.community.platform.shared.security.SecurityUtils;
import com.community.platform.user.application.AuthService;
import com.community.platform.user.application.UserMapper;
import com.community.platform.user.application.UserService;
import com.community.platform.user.domain.User;
import com.community.platform.user.dto.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 REST API Controller
 * 로그인, 토큰 갱신, 로그아웃 등 인증 관련 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * 사용자 로그인
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletResponse response) {
        log.info("사용자 로그인 요청: email={}", request.getEmail());

        // 인증 및 토큰 생성
        AuthService.TokenInfo tokenInfo = authService.login(request.getEmail(), request.getPassword());

        // 사용자 정보 조회
        User user = userService.getUserByEmail(request.getEmail());
        UserResponse userResponse = userMapper.toUserResponse(user);

        // 액세스 토큰을 HTTP-Only 쿠키로 설정
        Cookie accessTokenCookie = createTokenCookie("accessToken", tokenInfo.getAccessToken(), 3600); // 1시간
        response.addCookie(accessTokenCookie);

        // 리프레시 토큰을 HTTP-Only 쿠키로 설정
        Cookie refreshTokenCookie = createTokenCookie("refreshToken", tokenInfo.getRefreshToken(), 604800); // 7일
        response.addCookie(refreshTokenCookie);

        log.info("쿠키로 토큰 설정 완료: userId={}", user.getId());

        // 응답 생성 (토큰은 쿠키로 전달되므로 응답에서 제외)
        AuthResponse authResponse = AuthResponse.of(
            null,  // 액세스 토큰은 쿠키로만 전달
            null,  // 리프레시 토큰은 쿠키로만 전달
            tokenInfo.getTokenType(),
            tokenInfo.getExpiresIn(),
            userResponse
        );

        return ApiResponse.success(authResponse, "로그인이 완료되었습니다");
    }

    /**
     * 토큰 쿠키 생성 헬퍼 메서드
     */
    private Cookie createTokenCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);  // JavaScript 접근 불가
        cookie.setSecure(false);   // HTTPS에서만 전송 (프로덕션에서는 true로 설정)
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);  // 초 단위
        cookie.setAttribute("SameSite", "Lax");  // CSRF 방어
        return cookie;
    }

    /**
     * 토큰 갱신 (Refresh)
     * POST /api/v1/auth/refresh
     */
    @PostMapping("/refresh")
    public ApiResponse<AuthService.TokenInfo> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("토큰 갱신 요청");

        AuthService.TokenInfo tokenInfo = authService.refresh(request.getRefreshToken());
        return ApiResponse.success(tokenInfo, "토큰이 갱신되었습니다");
    }

    /**
     * 로그아웃
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        Long currentUserId = SecurityUtils.requireCurrentUserId();
        log.info("사용자 로그아웃: userId={}", currentUserId);

        authService.logout();

        // 액세스 토큰 쿠키 제거
        Cookie accessTokenCookie = createTokenCookie("accessToken", null, 0);
        response.addCookie(accessTokenCookie);

        // 리프레시 토큰 쿠키 제거
        Cookie refreshTokenCookie = createTokenCookie("refreshToken", null, 0);
        response.addCookie(refreshTokenCookie);

        log.info("쿠키 제거 완료: userId={}", currentUserId);

        return ApiResponse.success("로그아웃이 완료되었습니다");
    }

    /**
     * 현재 사용자 정보 조회 (토큰 검증)
     * GET /api/v1/auth/me
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponse> getCurrentUser() {
        Long currentUserId = SecurityUtils.requireCurrentUserId();
        log.debug("현재 사용자 정보 조회: userId={}", currentUserId);

        UserService.UserWithProfile userWithProfile = userService.getUserWithProfile(currentUserId);
        UserResponse response = userMapper.toUserResponseWithProfile(
            userWithProfile.getUser(), 
            userWithProfile.getProfile()
        );

        return ApiResponse.success(response);
    }

    /**
     * 토큰 유효성 검증
     * POST /api/v1/auth/validate
     */
    @PostMapping("/validate")
    public ApiResponse<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            boolean isValid = authService.validateToken(token);
            
            return ApiResponse.success(isValid, 
                isValid ? "유효한 토큰입니다" : "유효하지 않은 토큰입니다");
        }
        
        return ApiResponse.success(false, "토큰이 없습니다");
    }
}