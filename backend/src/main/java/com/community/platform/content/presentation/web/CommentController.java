package com.community.platform.content.presentation.web;

import com.community.platform.content.application.CommentService;
import com.community.platform.content.application.ContentMapper;
import com.community.platform.content.domain.Comment;
import com.community.platform.content.dto.CommentCreateRequest;
import com.community.platform.content.dto.CommentResponse;
import com.community.platform.content.dto.CommentUpdateRequest;
import com.community.platform.shared.dto.ApiResponse;
import com.community.platform.shared.dto.PageResponse;
import com.community.platform.user.application.UserMapper;
import com.community.platform.user.application.UserService;
import com.community.platform.user.dto.UserSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 댓글 관리 REST API Controller
 * 댓글/대댓글 CRUD 및 계층형 구조 관리 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;
    private final ContentMapper contentMapper;
    private final UserMapper userMapper;

    /**
     * 댓글 작성
     * POST /api/v1/comments
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> createComment(
            @RequestParam Long currentUserId, // TODO: Security 적용 후 제거
            @Valid @RequestBody CommentCreateRequest request) {
        log.info("댓글 작성: authorId={}, postId={}", currentUserId, request.getPostId());

        Comment comment;
        if (request.getParentCommentId() != null) {
            // 대댓글 작성
            comment = commentService.createReply(
                request.getPostId(), 
                request.getParentCommentId(), 
                currentUserId, 
                request.getContent()
            );
        } else {
            // 최상위 댓글 작성
            comment = commentService.createComment(
                request.getPostId(), 
                currentUserId, 
                request.getContent()
            );
        }

        CommentResponse response = buildCommentResponse(comment, currentUserId);
        return ApiResponse.success(response, "댓글이 작성되었습니다");
    }

    /**
     * 댓글 수정
     * PUT /api/v1/comments/{commentId}
     */
    @PutMapping("/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestParam Long currentUserId,
            @Valid @RequestBody CommentUpdateRequest request) {
        log.info("댓글 수정: commentId={}, authorId={}", commentId, currentUserId);

        commentService.updateComment(commentId, currentUserId, request.getContent());
        
        Comment comment = commentService.getCommentById(commentId);
        CommentResponse response = buildCommentResponse(comment, currentUserId);

        return ApiResponse.success(response, "댓글이 수정되었습니다");
    }

    /**
     * 댓글 삭제
     * DELETE /api/v1/comments/{commentId}
     */
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long currentUserId) {
        log.info("댓글 삭제: commentId={}, authorId={}", commentId, currentUserId);

        commentService.deleteComment(commentId, currentUserId);
        return ApiResponse.success("댓글이 삭제되었습니다");
    }

    /**
     * 댓글 상세 조회
     * GET /api/v1/comments/{commentId}
     */
    @GetMapping("/{commentId}")
    public ApiResponse<CommentResponse> getComment(
            @PathVariable Long commentId,
            @RequestParam(required = false) Long currentUserId) {
        log.debug("댓글 상세 조회: commentId={}", commentId);

        Comment comment = commentService.getCommentById(commentId);
        CommentResponse response = buildCommentResponse(comment, currentUserId);

        return ApiResponse.success(response);
    }

    /**
     * 게시글의 댓글 목록 조회 (계층형 구조)
     * GET /api/v1/posts/{postId}/comments
     */
    @GetMapping("/posts/{postId}")
    public ApiResponse<List<CommentResponse>> getCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(required = false) Long currentUserId) {
        log.debug("게시글 댓글 목록 조회: postId={}", postId);

        List<Comment> comments = commentService.getCommentsByPost(postId);
        List<CommentResponse> response = buildCommentsHierarchy(comments, currentUserId);

        return ApiResponse.success(response);
    }

    /**
     * 게시글의 최상위 댓글만 조회
     * GET /api/v1/posts/{postId}/comments/root
     */
    @GetMapping("/posts/{postId}/root")
    public ApiResponse<List<CommentResponse>> getRootCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(required = false) Long currentUserId) {
        log.debug("게시글 최상위 댓글 조회: postId={}", postId);

        List<Comment> comments = commentService.getRootCommentsByPost(postId);
        List<CommentResponse> response = comments.stream()
                .map(comment -> buildCommentResponse(comment, currentUserId))
                .toList();

        return ApiResponse.success(response);
    }

    /**
     * 특정 댓글의 대댓글 목록 조회
     * GET /api/v1/comments/{parentCommentId}/replies
     */
    @GetMapping("/{parentCommentId}/replies")
    public ApiResponse<List<CommentResponse>> getReplies(
            @PathVariable Long parentCommentId,
            @RequestParam(required = false) Long currentUserId) {
        log.debug("대댓글 목록 조회: parentCommentId={}", parentCommentId);

        List<Comment> replies = commentService.getRepliesByParentComment(parentCommentId);
        List<CommentResponse> response = replies.stream()
                .map(comment -> buildCommentResponse(comment, currentUserId))
                .toList();

        return ApiResponse.success(response);
    }

    /**
     * 작성자별 댓글 목록 조회
     * GET /api/v1/comments/author/{authorId}
     */
    @GetMapping("/author/{authorId}")
    public ApiResponse<PageResponse<CommentResponse>> getCommentsByAuthor(
            @PathVariable Long authorId,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Long currentUserId) {
        log.debug("작성자별 댓글 조회: authorId={}", authorId);

        Page<Comment> comments = commentService.getCommentsByAuthor(authorId, pageable);
        Page<CommentResponse> commentResponses = comments.map(comment -> 
            buildCommentResponse(comment, currentUserId));
        PageResponse<CommentResponse> response = PageResponse.of(commentResponses);

        return ApiResponse.success(response);
    }

    /**
     * 댓글 검색 (내용 기반)
     * GET /api/v1/comments/search
     */
    @GetMapping("/search")
    public ApiResponse<PageResponse<CommentResponse>> searchComments(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Long currentUserId) {
        log.debug("댓글 검색: keyword={}", keyword);

        Page<Comment> comments = commentService.searchComments(keyword, pageable);
        Page<CommentResponse> commentResponses = comments.map(comment -> 
            buildCommentResponse(comment, currentUserId));
        PageResponse<CommentResponse> response = PageResponse.of(commentResponses);

        return ApiResponse.success(response);
    }

    /**
     * 최근 댓글 조회 (관리자용)
     * GET /api/v1/comments/recent
     */
    @GetMapping("/recent")
    public ApiResponse<PageResponse<CommentResponse>> getRecentComments(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Long currentUserId) {
        log.debug("최근 댓글 조회");

        Page<Comment> comments = commentService.getRecentComments(pageable);
        Page<CommentResponse> commentResponses = comments.map(comment -> 
            buildCommentResponse(comment, currentUserId));
        PageResponse<CommentResponse> response = PageResponse.of(commentResponses);

        return ApiResponse.success(response);
    }

    /**
     * 댓글 차단 (관리자 전용)
     * POST /api/v1/comments/{commentId}/block
     */
    @PostMapping("/{commentId}/block")
    public ApiResponse<Void> blockComment(@PathVariable Long commentId) {
        log.info("댓글 차단: commentId={}", commentId);

        commentService.blockComment(commentId);
        return ApiResponse.success("댓글이 차단되었습니다");
    }

    /**
     * 댓글 복원 (관리자 전용)
     * POST /api/v1/comments/{commentId}/restore
     */
    @PostMapping("/{commentId}/restore")
    public ApiResponse<Void> restoreComment(@PathVariable Long commentId) {
        log.info("댓글 복원: commentId={}", commentId);

        commentService.restoreComment(commentId);
        return ApiResponse.success("댓글이 복원되었습니다");
    }

    // ========== Private Helper Methods ==========

    /**
     * CommentResponse 구성 (작성자 정보, 권한 정보 포함)
     */
    private CommentResponse buildCommentResponse(Comment comment, Long currentUserId) {
        // 작성자 정보 조회
        UserSummaryResponse author = getUserSummaryResponse(comment.getAuthorId());

        // 사용자별 상호작용 정보
        Boolean isLikedByUser = currentUserId != null ? false : null; // TODO: 댓글 좋아요 상태 조회
        Boolean isAuthor = currentUserId != null && currentUserId.equals(comment.getAuthorId());

        return contentMapper.toCommentResponseWithDetails(
            comment, author, null, isLikedByUser, isAuthor);
    }

    /**
     * 댓글 계층 구조 구성 (최상위 댓글 + 대댓글)
     */
    private List<CommentResponse> buildCommentsHierarchy(List<Comment> comments, Long currentUserId) {
        // 실제로는 더 복잡한 계층 구조 로직이 필요
        // 여기서는 단순화된 구현
        return comments.stream()
                .filter(Comment::isRootComment) // 최상위 댓글만
                .map(comment -> {
                    CommentResponse response = buildCommentResponse(comment, currentUserId);
                    
                    // 대댓글 조회 및 설정
                    List<Comment> replies = commentService.getRepliesByParentComment(comment.getId());
                    List<CommentResponse> replyResponses = replies.stream()
                            .map(reply -> buildCommentResponse(reply, currentUserId))
                            .toList();
                    
                    return CommentResponse.builder()
                            .id(response.getId())
                            .postId(response.getPostId())
                            .parentCommentId(response.getParentCommentId())
                            .author(response.getAuthor())
                            .content(response.getContent())
                            .status(response.getStatus())
                            .likeCount(response.getLikeCount())
                            .depth(response.getDepth())
                            .createdAt(response.getCreatedAt())
                            .updatedAt(response.getUpdatedAt())
                            .replies(replyResponses) // 대댓글 설정
                            .isLikedByUser(response.getIsLikedByUser())
                            .isAuthor(response.getIsAuthor())
                            .build();
                })
                .toList();
    }

    /**
     * 사용자 요약 정보 조회
     */
    private UserSummaryResponse getUserSummaryResponse(Long userId) {
        return userMapper.toUserSummaryResponse(userService.getUserById(userId));
    }
}