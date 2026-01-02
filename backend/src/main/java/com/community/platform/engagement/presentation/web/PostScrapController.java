package com.community.platform.engagement.presentation.web;

import com.community.platform.content.application.ContentMapper;
import com.community.platform.content.domain.Post;
import com.community.platform.content.dto.PostSummaryResponse;
import com.community.platform.content.infrastructure.persistence.PostRepository;
import com.community.platform.engagement.application.EngagementMapper;
import com.community.platform.engagement.application.PostScrapService;
import com.community.platform.engagement.domain.PostScrap;
import com.community.platform.engagement.domain.ScrapFolder;
import com.community.platform.engagement.dto.PostScrapRequest;
import com.community.platform.engagement.dto.PostScrapResponse;
import com.community.platform.engagement.dto.ScrapFolderResponse;
import com.community.platform.shared.dto.ApiResponse;
import com.community.platform.shared.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 게시글 스크랩 관리 REST API Controller
 * 게시글 스크랩/취소, 스크랩 폴더별 관리 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostScrapController {

    private final PostScrapService postScrapService;
    private final EngagementMapper engagementMapper;
    private final PostRepository postRepository;
    private final ContentMapper contentMapper;

    /**
     * 게시글 스크랩 추가
     * POST /api/v1/posts/{postId}/scrap
     */
    @PostMapping("/{postId}/scrap")
    public ApiResponse<PostScrapResponse> scrapPost(
            @PathVariable Long postId,
            @RequestParam Long currentUserId, // TODO: Security 적용 후 @AuthenticationPrincipal 사용
            @Valid @RequestBody PostScrapRequest request) {
        log.info("게시글 스크랩: postId={}, userId={}, folderId={}", 
            postId, currentUserId, request.getScrapFolderId());

        PostScrap scrap = postScrapService.scrapPost(
            currentUserId, 
            postId, 
            request.getScrapFolderId()
        );
        
        PostScrapResponse response = engagementMapper.toPostScrapResponse(scrap);
        return ApiResponse.success(response, "게시글이 스크랩되었습니다");
    }

    /**
     * 게시글 스크랩 취소
     * DELETE /api/v1/posts/{postId}/scrap
     */
    @DeleteMapping("/{postId}/scrap")
    public ApiResponse<Void> unscrapPost(
            @PathVariable Long postId,
            @RequestParam Long currentUserId) {
        log.info("게시글 스크랩 취소: postId={}, userId={}", postId, currentUserId);

        postScrapService.unscrapPost(currentUserId, postId);
        return ApiResponse.success("스크랩이 취소되었습니다");
    }

    /**
     * 게시글 스크랩 상태 확인
     * GET /api/v1/posts/{postId}/scrap/status
     */
    @GetMapping("/{postId}/scrap/status")
    public ApiResponse<Boolean> getScrapStatus(
            @PathVariable Long postId,
            @RequestParam Long currentUserId) {
        log.debug("게시글 스크랩 상태 확인: postId={}, userId={}", postId, currentUserId);

        boolean isScrapped = postScrapService.isScrapedByUser(currentUserId, postId);
        return ApiResponse.success(isScrapped);
    }

    /**
     * 게시글 총 스크랩 수 조회
     * GET /api/v1/posts/{postId}/scrap/count
     */
    @GetMapping("/{postId}/scrap/count")
    public ApiResponse<Long> getScrapCount(@PathVariable Long postId) {
        log.debug("게시글 스크랩 수 조회: postId={}", postId);

        Long scrapCount = postScrapService.getScrapCount(postId);
        return ApiResponse.success(scrapCount);
    }

    /**
     * 사용자의 전체 스크랩 목록 조회 (페이징)
     * GET /api/v1/scraps/me
     */
    @GetMapping("/scraps/me")
    public ApiResponse<PageResponse<PostScrapResponse>> getUserScraps(
            @RequestParam Long currentUserId, // TODO: Security 적용 후 제거
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("사용자 전체 스크랩 목록 조회: userId={}, page={}",
            currentUserId, pageable.getPageNumber());

        Page<PostScrap> scraps = postScrapService.getUserScraps(currentUserId, pageable);

        // 게시글 정보 조회 (N+1 방지)
        List<Long> postIds = scraps.getContent().stream()
                .map(PostScrap::getPostId)
                .collect(Collectors.toList());

        Map<Long, Post> postMap = postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        // PostScrapResponse 변환 (post 정보 포함)
        Page<PostScrapResponse> scrapResponses = scraps.map(scrap ->
            buildPostScrapResponse(scrap, postMap.get(scrap.getPostId()))
        );

        PageResponse<PostScrapResponse> response = PageResponse.of(scrapResponses);

        return ApiResponse.success(response);
    }

    /**
     * 특정 폴더의 스크랩 목록 조회 (페이징)
     * GET /api/v1/scrap-folders/{folderId}/scraps
     */
    @GetMapping("/scrap-folders/{folderId}/scraps")
    public ApiResponse<PageResponse<PostScrapResponse>> getFolderScraps(
            @PathVariable Long folderId,
            @RequestParam Long currentUserId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("폴더별 스크랩 목록 조회: folderId={}, userId={}, page={}",
            folderId, currentUserId, pageable.getPageNumber());

        Page<PostScrap> scraps = postScrapService.getScrapsByFolder(currentUserId, folderId, pageable);

        // 게시글 정보 조회 (N+1 방지)
        List<Long> postIds = scraps.getContent().stream()
                .map(PostScrap::getPostId)
                .collect(Collectors.toList());

        Map<Long, Post> postMap = postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        // PostScrapResponse 변환 (post 정보 포함)
        Page<PostScrapResponse> scrapResponses = scraps.map(scrap ->
            buildPostScrapResponse(scrap, postMap.get(scrap.getPostId()))
        );

        PageResponse<PostScrapResponse> response = PageResponse.of(scrapResponses);

        return ApiResponse.success(response);
    }

    /**
     * 스크랩을 다른 폴더로 이동
     * PUT /api/v1/posts/{postId}/scrap/move
     */
    @PutMapping("/{postId}/scrap/move")
    public ApiResponse<Void> moveScrapToFolder(
            @PathVariable Long postId,
            @RequestParam Long currentUserId,
            @RequestParam Long targetFolderId) {
        log.info("스크랩 폴더 이동: postId={}, userId={}, targetFolderId={}", 
            postId, currentUserId, targetFolderId);

        postScrapService.moveScrapToFolder(currentUserId, postId, targetFolderId);
        return ApiResponse.success("스크랩이 다른 폴더로 이동되었습니다");
    }

    /**
     * 스크랩 검색 (제목, 내용, 태그 기반)
     * GET /api/v1/scraps/me/search
     */
    @GetMapping("/scraps/me/search")
    public ApiResponse<PageResponse<PostScrapResponse>> searchUserScraps(
            @RequestParam Long currentUserId,
            @RequestParam String keyword,
            @RequestParam(required = false) Long folderId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("사용자 스크랩 검색: userId={}, keyword={}, folderId={}",
            currentUserId, keyword, folderId);

        Page<PostScrap> scraps = postScrapService.searchUserScraps(
            currentUserId, keyword, folderId, pageable);

        // 게시글 정보 조회 (N+1 방지)
        List<Long> postIds = scraps.getContent().stream()
                .map(PostScrap::getPostId)
                .collect(Collectors.toList());

        Map<Long, Post> postMap = postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        Page<PostScrapResponse> scrapResponses = scraps.map(scrap ->
            buildPostScrapResponse(scrap, postMap.get(scrap.getPostId()))
        );
        PageResponse<PostScrapResponse> response = PageResponse.of(scrapResponses);

        return ApiResponse.success(response);
    }

    /**
     * 중복 스크랩된 게시글 목록 조회 (같은 게시글을 여러 폴더에 스크랩)
     * GET /api/v1/scraps/me/duplicates
     */
    @GetMapping("/scraps/me/duplicates")
    public ApiResponse<PageResponse<Object[]>> getDuplicateScraps(
            @RequestParam Long currentUserId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("중복 스크랩 목록 조회: userId={}", currentUserId);

        Page<Object[]> duplicates = postScrapService.getDuplicateScraps(currentUserId, pageable);
        PageResponse<Object[]> response = PageResponse.of(duplicates);

        return ApiResponse.success(response);
    }

    /**
     * 최근 스크랩된 게시글 목록 조회
     * GET /api/v1/scraps/me/recent
     */
    @GetMapping("/scraps/me/recent")
    public ApiResponse<PageResponse<PostScrapResponse>> getRecentScraps(
            @RequestParam Long currentUserId,
            @RequestParam(defaultValue = "30") int days,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("최근 스크랩 목록 조회: userId={}, days={}", currentUserId, days);

        Page<PostScrap> scraps = postScrapService.getRecentScraps(currentUserId, days, pageable);

        // 게시글 정보 조회 (N+1 방지)
        List<Long> postIds = scraps.getContent().stream()
                .map(PostScrap::getPostId)
                .collect(Collectors.toList());

        Map<Long, Post> postMap = postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        Page<PostScrapResponse> scrapResponses = scraps.map(scrap ->
            buildPostScrapResponse(scrap, postMap.get(scrap.getPostId()))
        );
        PageResponse<PostScrapResponse> response = PageResponse.of(scrapResponses);

        return ApiResponse.success(response);
    }

    /**
     * 스크랩 활동 통계 조회
     * GET /api/v1/scraps/me/statistics
     */
    @GetMapping("/scraps/me/statistics")
    public ApiResponse<Object[]> getUserScrapStatistics(
            @RequestParam Long currentUserId,
            @RequestParam(defaultValue = "30") int days) {
        log.debug("사용자 스크랩 통계 조회: userId={}, days={}", currentUserId, days);

        Object[] statistics = postScrapService.getUserScrapStatistics(currentUserId, days);
        return ApiResponse.success(statistics);
    }

    // ========== Admin APIs (관리자 전용) ==========

    /**
     * 인기 스크랩 게시글 조회 (관리자/분석용)
     * GET /api/v1/admin/scraps/popular
     */
    @GetMapping("/admin/scraps/popular")
    public ApiResponse<PageResponse<Object[]>> getPopularScrappedPosts(
            @RequestParam(defaultValue = "30") int days,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("인기 스크랩 게시글 조회: days={}, page={}", days, pageable.getPageNumber());

        Page<Object[]> popularScraps = postScrapService.getPopularScrappedPosts(days, pageable);
        PageResponse<Object[]> response = PageResponse.of(popularScraps);

        return ApiResponse.success(response);
    }

    /**
     * 스크랩 활동 통계 조회 (관리자 전용)
     * GET /api/v1/admin/scraps/statistics
     */
    @GetMapping("/admin/scraps/statistics")
    public ApiResponse<Object[]> getScrapStatistics(
            @RequestParam(defaultValue = "30") int days) {
        log.debug("전체 스크랩 통계 조회: days={}", days);

        Object[] statistics = postScrapService.getScrapStatistics(days);
        return ApiResponse.success(statistics);
    }

    // ========== Helper Methods ==========

    /**
     * PostScrap을 PostScrapResponse로 변환 (게시글 정보 포함)
     */
    private PostScrapResponse buildPostScrapResponse(PostScrap scrap, Post post) {
        PostScrapResponse.PostScrapResponseBuilder builder = PostScrapResponse.builder()
                .id(scrap.getId())
                .postId(scrap.getPostId())
                .userId(scrap.getUserId())
                .folderId(scrap.getScrapFolder() != null ? scrap.getScrapFolder().getId() : null)
                .createdAt(scrap.getCreatedAt());

        // post 정보 추가
        if (post != null) {
            PostSummaryResponse postSummary = contentMapper.toPostSummaryResponse(post);
            builder.post(postSummary);
        }

        // scrapFolder 정보 추가
        if (scrap.getScrapFolder() != null) {
            ScrapFolderResponse folderResponse = engagementMapper.toScrapFolderResponse(scrap.getScrapFolder());
            builder.scrapFolder(folderResponse);
        }

        return builder.build();
    }
}