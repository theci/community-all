package com.community.platform.moderation.presentation.web;

import com.community.platform.moderation.application.ReportService;
import com.community.platform.moderation.domain.ReportStatus;
import com.community.platform.moderation.domain.ReportTargetType;
import com.community.platform.moderation.dto.*;
import com.community.platform.shared.dto.ApiResponse;
import com.community.platform.shared.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 신고 관리 REST API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 신고 생성
     * POST /api/v1/reports
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReportResponse> createReport(
            @RequestParam Long currentUserId,
            @Valid @RequestBody ReportCreateRequest request) {
        log.debug("신고 생성: userId={}, targetType={}, targetId={}",
                currentUserId, request.getTargetType(), request.getTargetId());

        ReportResponse response = reportService.createReport(currentUserId, request);

        return ApiResponse.success(response, "신고가 접수되었습니다");
    }

    /**
     * 신고 상세 조회
     * GET /api/v1/reports/{reportId}
     */
    @GetMapping("/{reportId}")
    public ApiResponse<ReportResponse> getReport(@PathVariable Long reportId) {
        log.debug("신고 상세 조회: reportId={}", reportId);

        ReportResponse response = reportService.getReport(reportId);

        return ApiResponse.success(response);
    }

    /**
     * 대기 중인 신고 목록 조회 (관리자용)
     * GET /api/v1/reports/pending
     */
    @GetMapping("/pending")
    public ApiResponse<PageResponse<ReportResponse>> getPendingReports(
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("대기 중인 신고 목록 조회");

        Page<ReportResponse> reports = reportService.getPendingReports(pageable);

        return ApiResponse.success(PageResponse.of(reports));
    }

    /**
     * 고위험 신고 목록 조회 (관리자용)
     * GET /api/v1/reports/high-severity
     */
    @GetMapping("/high-severity")
    public ApiResponse<PageResponse<ReportResponse>> getHighSeverityReports(
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("고위험 신고 목록 조회");

        Page<ReportResponse> reports = reportService.getHighSeverityReports(pageable);

        return ApiResponse.success(PageResponse.of(reports));
    }

    /**
     * 상태별 신고 목록 조회 (관리자용)
     * GET /api/v1/reports/status/{status}
     */
    @GetMapping("/status/{status}")
    public ApiResponse<PageResponse<ReportResponse>> getReportsByStatus(
            @PathVariable ReportStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("상태별 신고 목록 조회: status={}", status);

        Page<ReportResponse> reports = reportService.getReportsByStatus(status, pageable);

        return ApiResponse.success(PageResponse.of(reports));
    }

    /**
     * 대상 유형별 신고 목록 조회 (관리자용)
     * GET /api/v1/reports/type/{targetType}
     */
    @GetMapping("/type/{targetType}")
    public ApiResponse<PageResponse<ReportResponse>> getReportsByTargetType(
            @PathVariable ReportTargetType targetType,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("대상 유형별 신고 목록 조회: targetType={}", targetType);

        Page<ReportResponse> reports = reportService.getReportsByTargetType(targetType, pageable);

        return ApiResponse.success(PageResponse.of(reports));
    }

    /**
     * 특정 대상에 대한 신고 목록 조회
     * GET /api/v1/reports/target/{targetType}/{targetId}
     */
    @GetMapping("/target/{targetType}/{targetId}")
    public ApiResponse<List<ReportResponse>> getReportsByTarget(
            @PathVariable ReportTargetType targetType,
            @PathVariable Long targetId) {
        log.debug("특정 대상 신고 목록 조회: targetType={}, targetId={}", targetType, targetId);

        List<ReportResponse> reports = reportService.getReportsByTarget(targetType, targetId);

        return ApiResponse.success(reports);
    }

    /**
     * 내가 신고한 목록 조회
     * GET /api/v1/reports/me
     */
    @GetMapping("/me")
    public ApiResponse<PageResponse<ReportResponse>> getMyReports(
            @RequestParam Long currentUserId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("내 신고 목록 조회: userId={}", currentUserId);

        Page<ReportResponse> reports = reportService.getMyReports(currentUserId, pageable);

        return ApiResponse.success(PageResponse.of(reports));
    }

    /**
     * 특정 사용자에 대한 신고 목록 조회 (관리자용)
     * GET /api/v1/reports/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<PageResponse<ReportResponse>> getReportsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("특정 사용자 신고 목록 조회: userId={}", userId);

        Page<ReportResponse> reports = reportService.getReportsByUser(userId, pageable);

        return ApiResponse.success(PageResponse.of(reports));
    }

    /**
     * 신고 검토 시작 (관리자용)
     * POST /api/v1/reports/{reportId}/review/start
     */
    @PostMapping("/{reportId}/review/start")
    public ApiResponse<ReportResponse> startReview(
            @PathVariable Long reportId,
            @RequestParam Long currentUserId) {
        log.debug("신고 검토 시작: reportId={}, reviewerId={}", reportId, currentUserId);

        ReportResponse response = reportService.startReview(reportId, currentUserId);

        return ApiResponse.success(response, "신고 검토가 시작되었습니다");
    }

    /**
     * 신고 승인 (관리자용)
     * POST /api/v1/reports/{reportId}/approve
     */
    @PostMapping("/{reportId}/approve")
    public ApiResponse<ReportResponse> approveReport(
            @PathVariable Long reportId,
            @RequestParam Long currentUserId,
            @Valid @RequestBody ReportReviewRequest request) {
        log.debug("신고 승인: reportId={}, reviewerId={}", reportId, currentUserId);

        ReportResponse response = reportService.approveReport(reportId, currentUserId, request);

        return ApiResponse.success(response, "신고가 승인되었습니다");
    }

    /**
     * 신고 반려 (관리자용)
     * POST /api/v1/reports/{reportId}/reject
     */
    @PostMapping("/{reportId}/reject")
    public ApiResponse<ReportResponse> rejectReport(
            @PathVariable Long reportId,
            @RequestParam Long currentUserId,
            @Valid @RequestBody ReportReviewRequest request) {
        log.debug("신고 반려: reportId={}, reviewerId={}", reportId, currentUserId);

        ReportResponse response = reportService.rejectReport(reportId, currentUserId, request);

        return ApiResponse.success(response, "신고가 반려되었습니다");
    }

    /**
     * 신고 통계 조회 (관리자용)
     * GET /api/v1/reports/statistics
     */
    @GetMapping("/statistics")
    public ApiResponse<ReportStatisticsResponse> getReportStatistics() {
        log.debug("신고 통계 조회");

        ReportStatisticsResponse statistics = reportService.getReportStatistics();

        return ApiResponse.success(statistics);
    }

    /**
     * 기간별 신고 수 조회 (관리자용)
     * GET /api/v1/reports/statistics/count
     */
    @GetMapping("/statistics/count")
    public ApiResponse<Long> getReportCountByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("기간별 신고 수 조회: {} ~ {}", startDate, endDate);

        Long count = reportService.getReportCountByDateRange(startDate, endDate);

        return ApiResponse.success(count);
    }

    /**
     * 특정 대상의 신고 수 조회
     * GET /api/v1/reports/statistics/target/{targetType}/{targetId}/count
     */
    @GetMapping("/statistics/target/{targetType}/{targetId}/count")
    public ApiResponse<Long> getReportCountByTarget(
            @PathVariable ReportTargetType targetType,
            @PathVariable Long targetId) {
        log.debug("대상별 신고 수 조회: targetType={}, targetId={}", targetType, targetId);

        Long count = reportService.getReportCountByTarget(targetType, targetId);

        return ApiResponse.success(count);
    }
}
