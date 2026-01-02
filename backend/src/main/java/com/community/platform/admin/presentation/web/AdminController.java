package com.community.platform.admin.presentation.web;

import com.community.platform.admin.application.AdminService;
import com.community.platform.admin.dto.*;
import com.community.platform.authorization.domain.UserRole;
import com.community.platform.shared.dto.ApiResponse;
import com.community.platform.user.domain.UserStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * 관리자 통계 조회
     * GET /api/v1/admin/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<AdminStatisticsResponse>> getStatistics() {
        log.info("관리자 통계 조회");

        AdminStatisticsResponse statistics = adminService.getStatistics();

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 사용자 목록 조회 (검색, 필터링, 페이징)
     * GET /api/v1/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserManagementResponse>>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("사용자 목록 조회 - keyword: {}, status: {}, role: {}, page: {}, size: {}",
                keyword, status, role, page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserManagementResponse> users = adminService.getUserList(keyword, status, role, pageable);

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * 사용자 상세 조회
     * GET /api/v1/admin/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail(
            @PathVariable Long userId
    ) {
        log.info("사용자 상세 조회 - userId: {}", userId);

        UserDetailResponse userDetail = adminService.getUserDetail(userId);

        return ResponseEntity.ok(ApiResponse.success(userDetail));
    }

    /**
     * 사용자 상태 변경
     * PUT /api/v1/admin/users/{userId}/status
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        log.info("사용자 상태 변경 - userId: {}, status: {}", userId, request.getStatus());

        adminService.updateUserStatus(userId, request);

        return ResponseEntity.ok(ApiResponse.success("사용자 상태가 변경되었습니다"));
    }

    /**
     * 사용자 역할 변경
     * PUT /api/v1/admin/users/{userId}/role
     */
    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        log.info("사용자 역할 변경 - userId: {}, role: {}", userId, request.getRole());

        adminService.updateUserRole(userId, request);

        return ResponseEntity.ok(ApiResponse.success("사용자 역할이 변경되었습니다"));
    }

    /**
     * 사용자 포인트 조정
     * POST /api/v1/admin/users/{userId}/points
     */
    @PostMapping("/users/{userId}/points")
    public ResponseEntity<ApiResponse<Void>> adjustUserPoint(
            @PathVariable Long userId,
            @Valid @RequestBody AdjustUserPointRequest request
    ) {
        log.info("사용자 포인트 조정 - userId: {}, points: {}, reason: {}",
                userId, request.getPoints(), request.getReason());

        adminService.adjustUserPoint(userId, request);

        return ResponseEntity.ok(ApiResponse.success("사용자 포인트가 조정되었습니다"));
    }

    // ========== 콘텐츠 관리 (Content Moderation) ==========

    /**
     * 게시글 목록 조회 (관리자용 - 상태별, 검색)
     * GET /api/v1/admin/posts
     */
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<PostManagementResponse>>> getPostList(
            @RequestParam(required = false) com.community.platform.content.domain.PostStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isNotice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("게시글 목록 조회 - status: {}, keyword: {}, isNotice: {}, page: {}, size: {}",
                status, keyword, isNotice, page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PostManagementResponse> posts = adminService.getPostList(status, keyword, isNotice, pageable);

        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    /**
     * 게시글 상태 변경 (삭제/복구)
     * PUT /api/v1/admin/posts/{postId}/status
     */
    @PutMapping("/posts/{postId}/status")
    public ResponseEntity<ApiResponse<Void>> updatePostStatus(
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostStatusRequest request,
            @RequestParam Long adminId // TODO: Security 적용 후 @AuthenticationPrincipal 사용
    ) {
        log.info("게시글 상태 변경 - postId: {}, status: {}, adminId: {}", postId, request.getStatus(), adminId);

        adminService.updatePostStatus(postId, request, adminId);

        return ResponseEntity.ok(ApiResponse.success("게시글 상태가 변경되었습니다"));
    }

    /**
     * 게시글 공지사항 지정/해제
     * PUT /api/v1/admin/posts/{postId}/notice
     */
    @PutMapping("/posts/{postId}/notice")
    public ResponseEntity<ApiResponse<Void>> markPostAsNotice(
            @PathVariable Long postId,
            @Valid @RequestBody MarkAsNoticeRequest request
    ) {
        log.info("게시글 공지사항 지정/해제 - postId: {}, isNotice: {}", postId, request.getIsNotice());

        adminService.markPostAsNotice(postId, request);

        return ResponseEntity.ok(ApiResponse.success(
                request.getIsNotice() ? "공지사항으로 지정되었습니다" : "공지사항이 해제되었습니다"));
    }

    /**
     * 게시글 일괄 처리
     * POST /api/v1/admin/posts/bulk-action
     */
    @PostMapping("/posts/bulk-action")
    public ResponseEntity<ApiResponse<Void>> bulkPostAction(
            @Valid @RequestBody BulkPostActionRequest request,
            @RequestParam Long adminId // TODO: Security 적용 후 @AuthenticationPrincipal 사용
    ) {
        log.info("게시글 일괄 처리 - action: {}, count: {}, adminId: {}",
                request.getAction(), request.getPostIds().size(), adminId);

        adminService.bulkPostAction(request, adminId);

        return ResponseEntity.ok(ApiResponse.success("게시글이 일괄 처리되었습니다"));
    }

}
