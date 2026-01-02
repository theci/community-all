package com.community.platform.reward.presentation.web;

import com.community.platform.reward.application.PointService;
import com.community.platform.reward.application.RewardMapper;
import com.community.platform.reward.domain.PointTransaction;
import com.community.platform.reward.domain.UserLevel;
import com.community.platform.reward.domain.UserPoint;
import com.community.platform.reward.dto.*;
import com.community.platform.shared.dto.ApiResponse;
import com.community.platform.shared.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 포인트 관리 REST API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;
    private final RewardMapper rewardMapper;

    /**
     * 내 포인트 정보 조회
     * GET /api/v1/points/me
     */
    @GetMapping("/me")
    public ApiResponse<UserPointResponse> getMyPoints(@RequestParam Long currentUserId) {
        log.debug("내 포인트 조회: userId={}", currentUserId);

        UserPoint userPoint = pointService.getOrInitializeUserPoint(currentUserId);
        UserPointResponse response = rewardMapper.toUserPointResponse(userPoint);

        return ApiResponse.success(response);
    }

    /**
     * 내 포인트 거래 내역 조회
     * GET /api/v1/points/me/transactions
     */
    @GetMapping("/me/transactions")
    public ApiResponse<PageResponse<PointTransactionResponse>> getMyTransactions(
            @RequestParam Long currentUserId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("내 포인트 거래 내역 조회: userId={}", currentUserId);

        Page<PointTransaction> transactions = pointService.getUserTransactions(currentUserId, pageable);
        Page<PointTransactionResponse> response = transactions.map(rewardMapper::toPointTransactionResponse);

        return ApiResponse.success(PageResponse.of(response));
    }

    /**
     * 기간별 거래 내역 조회
     * GET /api/v1/points/me/transactions/period
     */
    @GetMapping("/me/transactions/period")
    public ApiResponse<PageResponse<PointTransactionResponse>> getMyTransactionsByPeriod(
            @RequestParam Long currentUserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("기간별 거래 내역 조회: userId={}, period={} to {}", currentUserId, startDate, endDate);

        Page<PointTransaction> transactions = pointService.getUserTransactionsByPeriod(
            currentUserId, startDate, endDate, pageable);
        Page<PointTransactionResponse> response = transactions.map(rewardMapper::toPointTransactionResponse);

        return ApiResponse.success(PageResponse.of(response));
    }

    /**
     * 포인트 사용
     * POST /api/v1/points/me/use
     */
    @PostMapping("/me/use")
    public ApiResponse<PointTransactionResponse> usePoints(
            @RequestParam Long currentUserId,
            @Valid @RequestBody PointUseRequest request) {
        log.info("포인트 사용: userId={}, points={}", currentUserId, request.getPoints());

        PointTransaction transaction = pointService.usePoints(
            currentUserId, request.getPoints(), request.getDescription());
        PointTransactionResponse response = rewardMapper.toPointTransactionResponse(transaction);

        return ApiResponse.success(response, "포인트가 사용되었습니다");
    }

    /**
     * 포인트 랭킹 조회
     * GET /api/v1/points/ranking
     */
    @GetMapping("/ranking")
    public ApiResponse<PageResponse<UserPointResponse>> getPointRanking(
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("포인트 랭킹 조회");

        Page<UserPoint> ranking = pointService.getPointRanking(pageable);
        Page<UserPointResponse> response = ranking.map(rewardMapper::toUserPointResponse);

        return ApiResponse.success(PageResponse.of(response));
    }

    /**
     * 레벨별 사용자 통계
     * GET /api/v1/points/statistics/levels
     */
    @GetMapping("/statistics/levels")
    public ApiResponse<Map<String, Long>> getLevelStatistics() {
        log.debug("레벨별 사용자 통계 조회");

        Map<String, Long> statistics = new HashMap<>();
        for (UserLevel level : UserLevel.values()) {
            long count = pointService.countUsersByLevel(level);
            statistics.put(level.name(), count);
        }

        return ApiResponse.success(statistics);
    }

    /**
     * 전체 포인트 통계
     * GET /api/v1/points/statistics/total
     */
    @GetMapping("/statistics/total")
    public ApiResponse<PointService.PointStatistics> getTotalStatistics() {
        log.debug("전체 포인트 통계 조회");

        PointService.PointStatistics statistics = pointService.getPointStatistics();
        return ApiResponse.success(statistics);
    }

    // ========== Admin APIs (관리자 전용) ==========

    /**
     * 관리자 포인트 지급/차감
     * POST /api/v1/admin/points/adjust
     */
    @PostMapping("/admin/adjust")
    public ApiResponse<PointTransactionResponse> adjustPoints(
            @RequestParam Long currentUserId, // 관리자 ID
            @Valid @RequestBody AdminPointAdjustRequest request) {
        log.info("관리자 포인트 조정: adminId={}, targetUserId={}, points={}",
                currentUserId, request.getTargetUserId(), request.getPoints());

        PointTransaction transaction = pointService.adminAdjustPoints(
            request.getTargetUserId(), currentUserId, request.getPoints(), request.getReason());
        PointTransactionResponse response = rewardMapper.toPointTransactionResponse(transaction);

        String message = request.getPoints() > 0 ? "포인트가 지급되었습니다" : "포인트가 차감되었습니다";
        return ApiResponse.success(response, message);
    }

    /**
     * 특정 사용자 포인트 조회 (관리자용)
     * GET /api/v1/admin/points/users/{userId}
     */
    @GetMapping("/admin/users/{userId}")
    public ApiResponse<UserPointResponse> getUserPoints(@PathVariable Long userId) {
        log.debug("사용자 포인트 조회 (관리자): userId={}", userId);

        UserPoint userPoint = pointService.getUserPoint(userId);
        UserPointResponse response = rewardMapper.toUserPointResponse(userPoint);

        return ApiResponse.success(response);
    }

    /**
     * 특정 레벨 이상 사용자 조회
     * GET /api/v1/admin/points/users/level/{level}
     */
    @GetMapping("/admin/users/level/{level}")
    public ApiResponse<PageResponse<UserPointResponse>> getUsersByLevel(
            @PathVariable UserLevel level,
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("레벨별 사용자 조회: level={}", level);

        Page<UserPoint> users = pointService.getUsersByLevelOrHigher(level, pageable);
        Page<UserPointResponse> response = users.map(rewardMapper::toUserPointResponse);

        return ApiResponse.success(PageResponse.of(response));
    }
}
