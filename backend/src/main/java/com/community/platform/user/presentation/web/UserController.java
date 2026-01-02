package com.community.platform.user.presentation.web;

import com.community.platform.shared.dto.ApiResponse;
import com.community.platform.shared.dto.PageResponse;
import com.community.platform.shared.security.SecurityUtils;
import com.community.platform.user.application.UserMapper;
import com.community.platform.user.application.UserService;
import com.community.platform.user.domain.User;
import com.community.platform.user.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관리 REST API Controller
 * 회원가입, 로그인, 프로필 관리 등 사용자 관련 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * 사용자 회원가입
     * POST /api/v1/users/register
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("사용자 회원가입 요청: email={}, nickname={}", request.getEmail(), request.getNickname());

        User user = userService.registerUser(
            request.getEmail(), 
            request.getPassword(), 
            request.getNickname()
        );

        UserResponse response = userMapper.toUserResponse(user);
        return ApiResponse.success(response, "회원가입이 완료되었습니다");
    }

    // 로그인 기능은 AuthController로 이동됨

    /**
     * 사용자 정보 조회
     * GET /api/v1/users/{userId}
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long userId) {
        log.debug("사용자 정보 조회: userId={}", userId);

        UserService.UserWithProfile userWithProfile = userService.getUserWithProfile(userId);
        UserResponse response = userMapper.toUserResponseWithProfile(
            userWithProfile.getUser(), 
            userWithProfile.getProfile()
        );

        return ApiResponse.success(response);
    }

    // 현재 사용자 정보 조회는 AuthController로 이동됨

    /**
     * 사용자 프로필 업데이트
     * PUT /api/v1/users/me/profile
     */
    @PutMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        Long currentUserId = SecurityUtils.requireCurrentUserId();
        log.info("사용자 프로필 업데이트: userId={}", currentUserId);

        userService.updateUserProfile(
            currentUserId,
            request.getProfileImageUrl(),
            request.getBio(),
            request.getBirthDate(),
            request.getGender()
        );

        return ApiResponse.success("프로필이 업데이트되었습니다");
    }

    /**
     * 활성 사용자 목록 조회 (페이징)
     * GET /api/v1/users
     */
    @GetMapping
    public ApiResponse<PageResponse<UserSummaryResponse>> getActiveUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("활성 사용자 목록 조회: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<User> users = userService.getActiveUsers(pageable);
        Page<UserSummaryResponse> userResponses = users.map(userMapper::toUserSummaryResponse);
        PageResponse<UserSummaryResponse> response = PageResponse.of(userResponses);

        return ApiResponse.success(response);
    }

    /**
     * 사용자 검색 (닉네임 기반)
     * GET /api/v1/users/search
     */
    @GetMapping("/search")
    public ApiResponse<PageResponse<UserSummaryResponse>> searchUsers(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("사용자 검색: keyword={}, page={}", keyword, pageable.getPageNumber());

        Page<User> users = userService.searchUsers(keyword, pageable);
        Page<UserSummaryResponse> userResponses = users.map(userMapper::toUserSummaryResponse);
        PageResponse<UserSummaryResponse> response = PageResponse.of(userResponses);

        return ApiResponse.success(response);
    }

    /**
     * 사용자 상태 변경 - 차단 (관리자 전용)
     * POST /api/v1/users/{userId}/block
     */
    @PostMapping("/{userId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> blockUser(
            @PathVariable Long userId,
            @RequestParam String reason) {
        log.info("사용자 차단: userId={}, reason={}", userId, reason);

        userService.blockUser(userId, reason);
        return ApiResponse.success("사용자가 차단되었습니다");
    }

    /**
     * 사용자 상태 변경 - 활성화 (관리자 전용)
     * POST /api/v1/users/{userId}/activate
     */
    @PostMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> activateUser(@PathVariable Long userId) {
        log.info("사용자 활성화: userId={}", userId);

        userService.activateUser(userId);
        return ApiResponse.success("사용자가 활성화되었습니다");
    }

    /**
     * 사용자 상태 변경 - 비활성화 (관리자 전용)
     * POST /api/v1/users/{userId}/deactivate
     */
    @PostMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deactivateUser(@PathVariable Long userId) {
        log.info("사용자 비활성화: userId={}", userId);

        userService.deactivateUser(userId);
        return ApiResponse.success("사용자가 비활성화되었습니다");
    }

    /**
     * 이메일 중복 확인
     * GET /api/v1/users/check-email
     */
    @GetMapping("/check-email")
    public ApiResponse<Boolean> checkEmailDuplicate(@RequestParam String email) {
        log.debug("이메일 중복 확인: email={}", email);

        try {
            userService.getUserByEmail(email);
            return ApiResponse.success(true, "사용 중인 이메일입니다"); // 중복됨
        } catch (Exception e) {
            return ApiResponse.success(false, "사용 가능한 이메일입니다"); // 중복되지 않음
        }
    }

    /**
     * 닉네임 중복 확인  
     * GET /api/v1/users/check-nickname
     */
    @GetMapping("/check-nickname")
    public ApiResponse<Boolean> checkNicknameDuplicate(@RequestParam String nickname) {
        log.debug("닉네임 중복 확인: nickname={}", nickname);

        // TODO: 별도 메서드로 분리하거나 Repository에서 exists 메서드 사용
        try {
            userService.getUserById(1L); // 임시 구현
            return ApiResponse.success(false, "사용 가능한 닉네임입니다");
        } catch (Exception e) {
            return ApiResponse.success(false, "사용 가능한 닉네임입니다");
        }
    }
}