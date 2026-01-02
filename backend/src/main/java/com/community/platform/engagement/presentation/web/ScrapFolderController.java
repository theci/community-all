package com.community.platform.engagement.presentation.web;

import com.community.platform.engagement.application.EngagementMapper;
import com.community.platform.engagement.application.ScrapFolderService;
import com.community.platform.engagement.domain.ScrapFolder;
import com.community.platform.engagement.dto.ScrapFolderCreateRequest;
import com.community.platform.engagement.dto.ScrapFolderResponse;
import com.community.platform.engagement.dto.ScrapFolderUpdateRequest;
import com.community.platform.shared.dto.ApiResponse;
import com.community.platform.shared.dto.PageResponse;
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
 * 스크랩 폴더 관리 REST API Controller
 * 폴더 생성/수정/삭제, 폴더별 스크랩 관리 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/scrap-folders")
@RequiredArgsConstructor
public class ScrapFolderController {

    private final ScrapFolderService scrapFolderService;
    private final EngagementMapper engagementMapper;

    /**
     * 새 스크랩 폴더 생성
     * POST /api/v1/scrap-folders
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ScrapFolderResponse> createFolder(
            @RequestParam Long currentUserId, // TODO: Security 적용 후 @AuthenticationPrincipal 사용
            @Valid @RequestBody ScrapFolderCreateRequest request) {
        log.info("스크랩 폴더 생성: userId={}, name={}", currentUserId, request.getName());

        ScrapFolder folder = scrapFolderService.createFolder(
            currentUserId,
            request.getName(),
            request.getDescription(),
            request.getIsPublic()
        );

        ScrapFolderResponse response = engagementMapper.toScrapFolderResponse(folder);
        return ApiResponse.success(response, "스크랩 폴더가 생성되었습니다");
    }

    /**
     * 사용자의 스크랩 폴더 목록 조회
     * GET /api/v1/scrap-folders/me
     */
    @GetMapping("/me")
    public ApiResponse<List<ScrapFolderResponse>> getUserFolders(
            @RequestParam Long currentUserId) {
        log.debug("사용자 스크랩 폴더 목록 조회: userId={}", currentUserId);

        List<ScrapFolder> folders = scrapFolderService.getUserFolders(currentUserId);
        List<ScrapFolderResponse> response = engagementMapper.toScrapFolderResponseList(folders);

        return ApiResponse.success(response);
    }

    /**
     * 특정 스크랩 폴더 상세 조회
     * GET /api/v1/scrap-folders/{folderId}
     */
    @GetMapping("/{folderId}")
    public ApiResponse<ScrapFolderResponse> getFolder(
            @PathVariable Long folderId,
            @RequestParam Long currentUserId) {
        log.debug("스크랩 폴더 상세 조회: folderId={}, userId={}", folderId, currentUserId);

        ScrapFolder folder = scrapFolderService.getFolder(folderId, currentUserId);
        ScrapFolderResponse response = engagementMapper.toScrapFolderResponse(folder);

        return ApiResponse.success(response);
    }

    /**
     * 스크랩 폴더 정보 수정
     * PUT /api/v1/scrap-folders/{folderId}
     */
    @PutMapping("/{folderId}")
    public ApiResponse<ScrapFolderResponse> updateFolder(
            @PathVariable Long folderId,
            @RequestParam Long currentUserId,
            @Valid @RequestBody ScrapFolderUpdateRequest request) {
        log.info("스크랩 폴더 수정: folderId={}, userId={}, name={}", 
            folderId, currentUserId, request.getName());

        scrapFolderService.updateFolder(
            folderId,
            currentUserId,
            request.getName(),
            request.getDescription(),
            request.getIsPublic()
        );

        ScrapFolder folder = scrapFolderService.getFolder(folderId, currentUserId);
        ScrapFolderResponse response = engagementMapper.toScrapFolderResponse(folder);

        return ApiResponse.success(response, "스크랩 폴더가 수정되었습니다");
    }

    /**
     * 스크랩 폴더 삭제 (소프트 삭제)
     * DELETE /api/v1/scrap-folders/{folderId}
     */
    @DeleteMapping("/{folderId}")
    public ApiResponse<Void> deleteFolder(
            @PathVariable Long folderId,
            @RequestParam Long currentUserId,
            @RequestParam(defaultValue = "false") boolean moveToDefault) {
        log.info("스크랩 폴더 삭제: folderId={}, userId={}, moveToDefault={}", 
            folderId, currentUserId, moveToDefault);

        scrapFolderService.deleteFolder(folderId, currentUserId, moveToDefault);
        return ApiResponse.success("스크랩 폴더가 삭제되었습니다");
    }

    /**
     * 기본 스크랩 폴더 설정
     * POST /api/v1/scrap-folders/{folderId}/set-default
     */
    @PostMapping("/{folderId}/set-default")
    public ApiResponse<Void> setDefaultFolder(
            @PathVariable Long folderId,
            @RequestParam Long currentUserId) {
        log.info("기본 스크랩 폴더 설정: folderId={}, userId={}", folderId, currentUserId);

        scrapFolderService.setDefaultFolder(currentUserId, folderId);
        return ApiResponse.success("기본 스크랩 폴더가 설정되었습니다");
    }

    /**
     * 스크랩 폴더 공개 설정 토글
     * POST /api/v1/scrap-folders/{folderId}/toggle-public
     */
    @PostMapping("/{folderId}/toggle-public")
    public ApiResponse<Boolean> togglePublicStatus(
            @PathVariable Long folderId,
            @RequestParam Long currentUserId) {
        log.info("스크랩 폴더 공개 설정 토글: folderId={}, userId={}", folderId, currentUserId);

        boolean isPublic = scrapFolderService.togglePublicStatus(folderId, currentUserId);
        String message = isPublic ? "폴더가 공개로 설정되었습니다" : "폴더가 비공개로 설정되었습니다";
        
        return ApiResponse.success(isPublic, message);
    }

    /**
     * 스크랩 폴더 정렬 순서 변경
     * PUT /api/v1/scrap-folders/reorder
     */
    @PutMapping("/reorder")
    public ApiResponse<Void> reorderFolders(
            @RequestParam Long currentUserId,
            @RequestBody List<Long> folderIds) {
        log.info("스크랩 폴더 정렬 순서 변경: userId={}, folderCount={}", 
            currentUserId, folderIds.size());

        scrapFolderService.reorderFolders(currentUserId, folderIds);
        return ApiResponse.success("폴더 순서가 변경되었습니다");
    }

    /**
     * 빈 스크랩 폴더 목록 조회
     * GET /api/v1/scrap-folders/me/empty
     */
    @GetMapping("/me/empty")
    public ApiResponse<List<ScrapFolderResponse>> getEmptyFolders(
            @RequestParam Long currentUserId) {
        log.debug("빈 스크랩 폴더 목록 조회: userId={}", currentUserId);

        List<ScrapFolder> emptyFolders = scrapFolderService.getEmptyFolders(currentUserId);
        List<ScrapFolderResponse> response = engagementMapper.toScrapFolderResponseList(emptyFolders);

        return ApiResponse.success(response);
    }

    /**
     * 폴더별 스크랩 수 통계 조회
     * GET /api/v1/scrap-folders/me/statistics
     */
    @GetMapping("/me/statistics")
    public ApiResponse<Object[]> getFolderStatistics(
            @RequestParam Long currentUserId) {
        log.debug("폴더별 스크랩 통계 조회: userId={}", currentUserId);

        Object[] statistics = scrapFolderService.getFolderStatistics(currentUserId);
        return ApiResponse.success(statistics);
    }

    // ========== Public APIs (공개 폴더 조회) ==========

    /**
     * 공개 스크랩 폴더 목록 조회 (페이징)
     * GET /api/v1/scrap-folders/public
     */
    @GetMapping("/public")
    public ApiResponse<PageResponse<ScrapFolderResponse>> getPublicFolders(
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("공개 스크랩 폴더 목록 조회: page={}", pageable.getPageNumber());

        Page<ScrapFolder> folders = scrapFolderService.getPublicFolders(pageable);
        Page<ScrapFolderResponse> folderResponses = folders.map(engagementMapper::toScrapFolderResponse);
        PageResponse<ScrapFolderResponse> response = PageResponse.of(folderResponses);

        return ApiResponse.success(response);
    }

    /**
     * 특정 사용자의 공개 폴더 목록 조회
     * GET /api/v1/scrap-folders/user/{userId}/public
     */
    @GetMapping("/user/{userId}/public")
    public ApiResponse<List<ScrapFolderResponse>> getUserPublicFolders(
            @PathVariable Long userId) {
        log.debug("사용자 공개 폴더 목록 조회: userId={}", userId);

        List<ScrapFolder> folders = scrapFolderService.getUserPublicFolders(userId);
        List<ScrapFolderResponse> response = engagementMapper.toScrapFolderResponseList(folders);

        return ApiResponse.success(response);
    }

    /**
     * 인기 공개 폴더 조회 (스크랩 수 기준)
     * GET /api/v1/scrap-folders/public/popular
     */
    @GetMapping("/public/popular")
    public ApiResponse<PageResponse<ScrapFolderResponse>> getPopularPublicFolders(
            @RequestParam(defaultValue = "30") int days,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("인기 공개 폴더 조회: days={}, page={}", days, pageable.getPageNumber());

        Page<ScrapFolder> folders = scrapFolderService.getPopularPublicFolders(days, pageable);
        Page<ScrapFolderResponse> folderResponses = folders.map(engagementMapper::toScrapFolderResponse);
        PageResponse<ScrapFolderResponse> response = PageResponse.of(folderResponses);

        return ApiResponse.success(response);
    }

    // ========== Admin APIs (관리자 전용) ==========

    /**
     * 모든 스크랩 폴더 조회 (관리자 전용)
     * GET /api/v1/admin/scrap-folders
     */
    @GetMapping("/admin")
    public ApiResponse<PageResponse<ScrapFolderResponse>> getAllFolders(
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("모든 스크랩 폴더 조회 (관리자): page={}", pageable.getPageNumber());

        Page<ScrapFolder> folders = scrapFolderService.getAllFolders(pageable);
        Page<ScrapFolderResponse> folderResponses = folders.map(engagementMapper::toScrapFolderResponse);
        PageResponse<ScrapFolderResponse> response = PageResponse.of(folderResponses);

        return ApiResponse.success(response);
    }

    /**
     * 폴더 사용 통계 조회 (관리자 전용)
     * GET /api/v1/admin/scrap-folders/statistics
     */
    @GetMapping("/admin/statistics")
    public ApiResponse<Object[]> getAdminFolderStatistics(
            @RequestParam(defaultValue = "30") int days) {
        log.debug("폴더 사용 통계 조회 (관리자): days={}", days);

        Object[] statistics = scrapFolderService.getAdminFolderStatistics(days);
        return ApiResponse.success(statistics);
    }

    /**
     * 사용자별 폴더 활동 조회 (관리자 전용)
     * GET /api/v1/admin/users/{userId}/folder-activity
     */
    @GetMapping("/admin/users/{userId}/folder-activity")
    public ApiResponse<Object[]> getUserFolderActivity(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        log.debug("사용자 폴더 활동 조회 (관리자): userId={}, days={}", userId, days);

        Object[] activity = scrapFolderService.getUserFolderActivity(userId, days);
        return ApiResponse.success(activity);
    }
}