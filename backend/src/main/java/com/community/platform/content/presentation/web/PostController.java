package com.community.platform.content.presentation.web;

import com.community.platform.content.application.ContentMapper;
import com.community.platform.content.application.PostService;
import com.community.platform.content.domain.Post;
import com.community.platform.content.dto.*;
import com.community.platform.shared.dto.ApiResponse;
import com.community.platform.shared.dto.PageResponse;
import com.community.platform.shared.security.SecurityUtils;
import com.community.platform.user.application.UserMapper;
import com.community.platform.user.application.UserService;
import com.community.platform.user.domain.User;
import com.community.platform.user.dto.UserSummaryResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 게시글 관리 REST API Controller
 * 게시글 CRUD, 검색, 발행 등 게시글 관련 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final ContentMapper contentMapper;
    private final UserMapper userMapper;

    /**
     * 게시글 작성
     * POST /api/v1/posts
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> createPost(
            @RequestParam Long currentUserId, // TODO: Security 적용 후 @AuthenticationPrincipal 사용
            @Valid @RequestBody PostCreateRequest request) {
        log.info("게시글 작성: authorId={}, title={}", currentUserId, request.getTitle());

        Post post = postService.createPost(
            currentUserId,
            request.getCategoryId(),
            request.getTitle(),
            request.getContent(),
            request.getContentType(),
            request.getTags()
        );

        PostResponse response = buildPostResponse(post, currentUserId);
        return ApiResponse.success(response, "게시글이 작성되었습니다");
    }

    /**
     * 게시글 발행 (임시저장 → 발행)
     * POST /api/v1/posts/{postId}/publish
     */
    @PostMapping("/{postId}/publish")
    public ApiResponse<Void> publishPost(
            @PathVariable Long postId,
            @RequestParam Long currentUserId) {
        log.info("게시글 발행: postId={}, authorId={}", postId, currentUserId);

        postService.publishPost(postId, currentUserId);
        return ApiResponse.success("게시글이 발행되었습니다");
    }

    /**
     * 게시글 수정
     * PUT /api/v1/posts/{postId}
     */
    @PutMapping("/{postId}")
    public ApiResponse<PostResponse> updatePost(
            @PathVariable Long postId,
            @RequestParam Long currentUserId,
            @Valid @RequestBody PostUpdateRequest request) {
        log.info("게시글 수정: postId={}, authorId={}", postId, currentUserId);

        postService.updatePost(postId, currentUserId, request.getTitle(), request.getContent(), request.getTags());
        
        Post post = postService.getPostById(postId);
        PostResponse response = buildPostResponse(post, currentUserId);
        
        return ApiResponse.success(response, "게시글이 수정되었습니다");
    }

    /**
     * 게시글 삭제
     * DELETE /api/v1/posts/{postId}
     */
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(
            @PathVariable Long postId,
            @RequestParam Long currentUserId) {
        log.info("게시글 삭제: postId={}, authorId={}", postId, currentUserId);

        postService.deletePost(postId, currentUserId);
        return ApiResponse.success("게시글이 삭제되었습니다");
    }

    /**
     * 게시글 상세 조회 (조회수 증가, Redis 중복 방지)
     * GET /api/v1/posts/{postId}
     */
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPost(
            @PathVariable Long postId,
            @RequestParam(required = false) Long currentUserId,
            HttpServletRequest request) {
        log.debug("게시글 상세 조회: postId={}", postId);

        String userId = currentUserId != null ? currentUserId.toString() : null;
        String ip = getClientIp(request);

        Post post = postService.getPostWithViewCount(postId, userId, ip);
        PostResponse response = buildPostResponse(post, currentUserId);

        return ApiResponse.success(response);
    }

    /**
     * 클라이언트 IP 주소 추출
     * X-Forwarded-For 헤더 우선, 없으면 Remote Address 사용
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 쉼표로 구분된 경우 첫 번째 IP만 사용
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 발행된 게시글 목록 조회 (메인 페이지)
     * GET /api/v1/posts
     */
    @GetMapping
    public ApiResponse<PageResponse<PostSummaryResponse>> getPosts(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Long currentUserId) {
        log.debug("게시글 목록 조회: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Post> posts = postService.getPublishedPosts(pageable);
        Page<PostSummaryResponse> postResponses = posts.map(post -> buildPostSummaryResponse(post, currentUserId));
        PageResponse<PostSummaryResponse> response = PageResponse.of(postResponses);

        return ApiResponse.success(response);
    }

    /**
     * 카테고리별 게시글 목록 조회
     * GET /api/v1/posts/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    public ApiResponse<PageResponse<PostSummaryResponse>> getPostsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Long currentUserId) {
        log.debug("카테고리별 게시글 조회: categoryId={}", categoryId);

        Page<Post> posts = postService.getPostsByCategory(categoryId, pageable);
        Page<PostSummaryResponse> postResponses = posts.map(post -> buildPostSummaryResponse(post, currentUserId));
        PageResponse<PostSummaryResponse> response = PageResponse.of(postResponses);

        return ApiResponse.success(response);
    }

    /**
     * 작성자별 게시글 목록 조회
     * GET /api/v1/posts/author/{authorId}
     */
    @GetMapping("/author/{authorId}")
    public ApiResponse<PageResponse<PostSummaryResponse>> getPostsByAuthor(
            @PathVariable Long authorId,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Long currentUserId) {
        log.debug("작성자별 게시글 조회: authorId={}", authorId);

        Page<Post> posts = postService.getPostsByAuthor(authorId, pageable);
        Page<PostSummaryResponse> postResponses = posts.map(post -> buildPostSummaryResponse(post, currentUserId));
        PageResponse<PostSummaryResponse> response = PageResponse.of(postResponses);

        return ApiResponse.success(response);
    }

    /**
     * 게시글 검색
     * GET /api/v1/posts/search
     */
    @GetMapping("/search")
    public ApiResponse<PageResponse<PostSummaryResponse>> searchPosts(
            @Valid @ModelAttribute PostSearchRequest searchRequest,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Long currentUserId) {
        log.debug("게시글 검색: keyword={}", searchRequest.getKeyword());

        Page<Post> posts = postService.searchPostsWithFilters(
            searchRequest.getKeyword(),
            searchRequest.getCategoryId(),
            searchRequest.getTagNames(),
            searchRequest.getStartDate(),
            searchRequest.getEndDate(),
            pageable
        );

        Page<PostSummaryResponse> postResponses = posts.map(post -> buildPostSummaryResponse(post, currentUserId));
        PageResponse<PostSummaryResponse> response = PageResponse.of(postResponses);

        return ApiResponse.success(response);
    }

    /**
     * 인기 게시글 조회
     * GET /api/v1/posts/popular
     */
    @GetMapping("/popular")
    public ApiResponse<PageResponse<PostSummaryResponse>> getPopularPosts(
            @RequestParam(defaultValue = "7") int days, // 기본 7일
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Long currentUserId) {
        log.debug("인기 게시글 조회: days={}", days);

        Page<Post> posts = postService.getPopularPosts(days, pageable);
        Page<PostSummaryResponse> postResponses = posts.map(post -> buildPostSummaryResponse(post, currentUserId));
        PageResponse<PostSummaryResponse> response = PageResponse.of(postResponses);

        return ApiResponse.success(response);
    }

    /**
     * 트렌딩 게시글 조회
     * GET /api/v1/posts/trending
     */
    @GetMapping("/trending")
    public ApiResponse<PageResponse<PostSummaryResponse>> getTrendingPosts(
            @RequestParam(defaultValue = "24") int hours, // 기본 24시간
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Long currentUserId) {
        log.debug("트렌딩 게시글 조회: hours={}", hours);

        Page<Post> posts = postService.getTrendingPosts(hours, pageable);
        Page<PostSummaryResponse> postResponses = posts.map(post -> buildPostSummaryResponse(post, currentUserId));
        PageResponse<PostSummaryResponse> response = PageResponse.of(postResponses);

        return ApiResponse.success(response);
    }

    /**
     * 공지사항 목록 조회
     * GET /api/v1/posts/notices
     */
    @GetMapping("/notices")
    public ApiResponse<List<PostSummaryResponse>> getNoticePosts(
            @RequestParam(required = false) Long currentUserId) {
        log.debug("공지사항 목록 조회");

        List<Post> posts = postService.getNoticePosts();
        List<PostSummaryResponse> response = posts.stream()
                .map(post -> buildPostSummaryResponse(post, currentUserId))
                .toList();

        return ApiResponse.success(response);
    }

    /**
     * 게시글을 공지사항으로 설정 (관리자 전용)
     * POST /api/v1/posts/{postId}/notice
     */
    @PostMapping("/{postId}/notice")
    public ApiResponse<Void> markAsNotice(@PathVariable Long postId) {
        log.info("게시글 공지사항 설정: postId={}", postId);

        postService.markAsNotice(postId);
        return ApiResponse.success("공지사항으로 설정되었습니다");
    }

    /**
     * 공지사항 해제 (관리자 전용)
     * DELETE /api/v1/posts/{postId}/notice
     */
    @DeleteMapping("/{postId}/notice")
    public ApiResponse<Void> unmarkAsNotice(@PathVariable Long postId) {
        log.info("공지사항 해제: postId={}", postId);

        postService.unmarkAsNotice(postId);
        return ApiResponse.success("공지사항이 해제되었습니다");
    }

    /**
     * 삭제된 게시글 목록 조회 (관리자 전용)
     * GET /api/v1/posts/deleted
     */
    @GetMapping("/deleted")
    public ApiResponse<PageResponse<PostSummaryResponse>> getDeletedPosts(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Long currentUserId) {
        log.info("삭제된 게시글 목록 조회");

        // TODO: 관리자 권한 체크

        Page<Post> posts = postService.getDeletedPosts(pageable);
        Page<PostSummaryResponse> postResponses = posts.map(post -> buildPostSummaryResponse(post, currentUserId));
        PageResponse<PostSummaryResponse> response = PageResponse.of(postResponses);

        return ApiResponse.success(response);
    }

    /**
     * 게시글 복구 (관리자 전용)
     * POST /api/v1/posts/{postId}/restore
     */
    @PostMapping("/{postId}/restore")
    public ApiResponse<Void> restorePost(
            @PathVariable Long postId,
            @RequestParam(required = false) Long currentUserId) {
        log.info("게시글 복구: postId={}, adminId={}", postId, currentUserId);

        // TODO: 관리자 권한 체크

        postService.restorePost(postId, currentUserId);
        return ApiResponse.success("게시글이 복구되었습니다");
    }

    /**
     * 유사한 게시글 조회
     * GET /api/v1/posts/{postId}/similar
     */
    @GetMapping("/{postId}/similar")
    public ApiResponse<List<PostSummaryResponse>> getSimilarPosts(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) Long currentUserId) {
        log.debug("유사한 게시글 조회: postId={}", postId);

        List<Post> posts = postService.getSimilarPosts(postId, limit);
        List<PostSummaryResponse> response = posts.stream()
                .map(post -> buildPostSummaryResponse(post, currentUserId))
                .toList();

        return ApiResponse.success(response);
    }

    // ========== Private Helper Methods ==========

    /**
     * PostResponse 구성 (작성자 정보, 카테고리, 태그 포함)
     */
    private PostResponse buildPostResponse(Post post, Long currentUserId) {
        // 작성자 정보 조회
        UserSummaryResponse author = getUserSummaryResponse(post.getAuthorId());

        // 카테고리 정보 조회 (lazy loading 문제 회피를 위해 직접 빌드)
        CategoryResponse category = CategoryResponse.builder()
                .id(post.getCategory().getId())
                .name(post.getCategory().getName())
                .description(post.getCategory().getDescription())
                .displayOrder(post.getCategory().getDisplayOrder())
                .isActive(post.getCategory().getIsActive())
                .createdAt(post.getCategory().getCreatedAt())
                .parentCategoryId(null) // TODO: parentCategory lazy loading 문제
                .subCategories(null)
                .postCount(null)
                .build();

        // 태그 정보 조회 (실제로는 PostTag를 통해 조회해야 함)
        List<TagResponse> tags = List.of(); // TODO: 태그 정보 조회 로직 구현

        // 사용자별 상호작용 정보 (로그인한 경우만)
        Boolean isLikedByUser = currentUserId != null ? false : null; // TODO: 좋아요 상태 조회
        Boolean isScrappedByUser = currentUserId != null ? false : null; // TODO: 스크랩 상태 조회

        return contentMapper.toPostResponseWithDetails(post, author, category, tags, isLikedByUser, isScrappedByUser);
    }

    /**
     * PostSummaryResponse 구성
     */
    private PostSummaryResponse buildPostSummaryResponse(Post post, Long currentUserId) {
        // 작성자 정보 조회
        UserSummaryResponse author = getUserSummaryResponse(post.getAuthorId());

        // 카테고리 정보 조회 (lazy loading 문제 회피를 위해 직접 빌드)
        CategoryResponse category = CategoryResponse.builder()
                .id(post.getCategory().getId())
                .name(post.getCategory().getName())
                .description(post.getCategory().getDescription())
                .displayOrder(post.getCategory().getDisplayOrder())
                .isActive(post.getCategory().getIsActive())
                .createdAt(post.getCategory().getCreatedAt())
                .parentCategoryId(null) // TODO: parentCategory lazy loading 문제
                .subCategories(null)
                .postCount(null)
                .build();

        // 태그 정보 조회 (실제로는 PostTag를 통해 조회해야 함)
        List<TagResponse> tags = List.of(); // TODO: 태그 정보 조회 로직 구현

        // 사용자별 상호작용 정보 (로그인한 경우만)
        Boolean isLikedByUser = currentUserId != null ? false : null; // TODO: 좋아요 상태 조회
        Boolean isScrappedByUser = currentUserId != null ? false : null; // TODO: 스크랩 상태 조회

        return contentMapper.toPostSummaryResponseWithDetails(post, author, category, tags, isLikedByUser, isScrappedByUser);
    }

    /**
     * 사용자 요약 정보 조회
     */
    private UserSummaryResponse getUserSummaryResponse(Long userId) {
        User user = userService.getUserById(userId);
        return UserSummaryResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(null) // TODO: profile lazy loading 문제로 인해 임시로 null 처리
                .build();
    }
}