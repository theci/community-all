package com.community.platform.user.presentation.web;

import com.community.platform.shared.dto.ApiResponse;
import com.community.platform.shared.security.SecurityUtils;
import com.community.platform.user.application.UserFollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 사용자 팔로우 REST API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserFollowService userFollowService;

    /**
     * 팔로우
     * POST /api/v1/users/{userId}/follow
     */
    @PostMapping("/{userId}/follow")
    public ApiResponse<Void> followUser(
            @PathVariable Long userId,
            @RequestParam Long currentUserId) {
        log.info("팔로우 요청: currentUserId={}, targetUserId={}", currentUserId, userId);

        userFollowService.follow(currentUserId, userId);
        return ApiResponse.success("팔로우했습니다");
    }

    /**
     * 언팔로우
     * DELETE /api/v1/users/{userId}/follow
     */
    @DeleteMapping("/{userId}/follow")
    public ApiResponse<Void> unfollowUser(
            @PathVariable Long userId,
            @RequestParam Long currentUserId) {
        log.info("언팔로우 요청: currentUserId={}, targetUserId={}", currentUserId, userId);

        userFollowService.unfollow(currentUserId, userId);
        return ApiResponse.success("언팔로우했습니다");
    }

    /**
     * 팔로우 상태 확인
     * GET /api/v1/users/{userId}/follow/status
     */
    @GetMapping("/{userId}/follow/status")
    public ApiResponse<Map<String, Boolean>> getFollowStatus(
            @PathVariable Long userId,
            @RequestParam Long currentUserId) {
        boolean isFollowing = userFollowService.isFollowing(currentUserId, userId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isFollowing", isFollowing);

        return ApiResponse.success(response);
    }

    /**
     * 팔로잉 목록 조회 (내가 팔로우하는 사용자 ID 목록)
     * GET /api/v1/users/{userId}/following
     */
    @GetMapping("/{userId}/following")
    public ApiResponse<List<Long>> getFollowingList(@PathVariable Long userId) {
        log.debug("팔로잉 목록 조회: userId={}", userId);

        List<Long> followingIds = userFollowService.getFollowingIds(userId);
        return ApiResponse.success(followingIds);
    }

    /**
     * 팔로워 목록 조회 (나를 팔로우하는 사용자 ID 목록)
     * GET /api/v1/users/{userId}/followers
     */
    @GetMapping("/{userId}/followers")
    public ApiResponse<List<Long>> getFollowerList(@PathVariable Long userId) {
        log.debug("팔로워 목록 조회: userId={}", userId);

        List<Long> followerIds = userFollowService.getFollowerIds(userId);
        return ApiResponse.success(followerIds);
    }

    /**
     * 팔로잉/팔로워 수 조회
     * GET /api/v1/users/{userId}/follow/count
     */
    @GetMapping("/{userId}/follow/count")
    public ApiResponse<Map<String, Long>> getFollowCount(@PathVariable Long userId) {
        log.debug("팔로우 카운트 조회: userId={}", userId);

        long followingCount = userFollowService.getFollowingCount(userId);
        long followerCount = userFollowService.getFollowerCount(userId);

        Map<String, Long> response = new HashMap<>();
        response.put("followingCount", followingCount);
        response.put("followerCount", followerCount);

        return ApiResponse.success(response);
    }
}
