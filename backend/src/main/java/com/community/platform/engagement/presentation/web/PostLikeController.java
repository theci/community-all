package com.community.platform.engagement.presentation.web;

import com.community.platform.engagement.application.EngagementMapper;
import com.community.platform.engagement.application.PostLikeService;
import com.community.platform.engagement.domain.PostLike;
import com.community.platform.engagement.dto.LikeToggleResponse;
import com.community.platform.engagement.dto.PostLikeResponse;
import com.community.platform.shared.dto.ApiResponse;
import com.community.platform.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 게시글 좋아요 관리 REST API Controller
 * 좋아요 추가/취소, 사용자별 좋아요 내역 조회 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;
    private final EngagementMapper engagementMapper;

    /**
     * 게시글 좋아요 토글 (추가/취소)
     * POST /api/v1/posts/{postId}/like
     */
    @PostMapping("/{postId}/like")
    public ApiResponse<LikeToggleResponse> toggleLike(
            @PathVariable Long postId,
            @RequestParam Long currentUserId) { // TODO: Security 적용 후 @AuthenticationPrincipal 사용
        log.info("게시글 좋아요 토글: postId={}, userId={}", postId, currentUserId);

        PostLikeService.LikeResult result = postLikeService.toggleLike(currentUserId, postId);
        LikeToggleResponse response = LikeToggleResponse.builder()
                .postId(postId)
                .isLiked(result.isLiked())
                .totalLikeCount(result.getTotalLikeCount())
                .message(result.isLiked() ? "좋아요를 누르셨습니다" : "좋아요를 취소하셨습니다")
                .build();

        return ApiResponse.success(response);
    }

    /**
     * 게시글 좋아요 상태 확인
     * GET /api/v1/posts/{postId}/like/status
     */
    @GetMapping("/{postId}/like/status")
    public ApiResponse<Boolean> getLikeStatus(
            @PathVariable Long postId,
            @RequestParam Long currentUserId) {
        log.debug("게시글 좋아요 상태 확인: postId={}, userId={}", postId, currentUserId);

        boolean isLiked = postLikeService.isLikedByUser(currentUserId, postId);
        return ApiResponse.success(isLiked);
    }

    /**
     * 게시글 총 좋아요 수 조회
     * GET /api/v1/posts/{postId}/like/count
     */
    @GetMapping("/{postId}/like/count")
    public ApiResponse<Long> getLikeCount(@PathVariable Long postId) {
        log.debug("게시글 좋아요 수 조회: postId={}", postId);

        Long likeCount = postLikeService.getLikeCount(postId);
        return ApiResponse.success(likeCount);
    }

    /**
     * 게시글 좋아요한 사용자 목록 조회 (페이징)
     * GET /api/v1/posts/{postId}/likes
     */
    @GetMapping("/{postId}/likes")
    public ApiResponse<PageResponse<PostLikeResponse>> getPostLikes(
            @PathVariable Long postId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("게시글 좋아요한 사용자 목록 조회: postId={}, page={}", 
            postId, pageable.getPageNumber());

        Page<PostLike> likes = postLikeService.getPostLikes(postId, pageable);
        Page<PostLikeResponse> likeResponses = likes.map(engagementMapper::toPostLikeResponse);
        PageResponse<PostLikeResponse> response = PageResponse.of(likeResponses);

        return ApiResponse.success(response);
    }

    /**
     * 사용자가 좋아요한 게시글 목록 조회 (페이징)
     * GET /api/v1/users/me/likes
     */
    @GetMapping("/likes/me")
    public ApiResponse<PageResponse<PostLikeResponse>> getUserLikes(
            @RequestParam Long currentUserId, // TODO: Security 적용 후 제거
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("사용자 좋아요 내역 조회: userId={}, page={}", 
            currentUserId, pageable.getPageNumber());

        Page<PostLike> likes = postLikeService.getUserLikes(currentUserId, pageable);
        Page<PostLikeResponse> likeResponses = likes.map(engagementMapper::toPostLikeResponse);
        PageResponse<PostLikeResponse> response = PageResponse.of(likeResponses);

        return ApiResponse.success(response);
    }

}