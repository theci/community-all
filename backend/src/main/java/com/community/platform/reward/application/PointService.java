package com.community.platform.reward.application;

import com.community.platform.reward.domain.*;
import com.community.platform.reward.exception.*;
import com.community.platform.reward.infrastructure.persistence.PointTransactionRepository;
import com.community.platform.reward.infrastructure.persistence.UserPointRepository;
import com.community.platform.user.exception.UserNotFoundException;
import com.community.platform.user.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 포인트 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * 사용자 포인트 초기화
     */
    @Transactional
    public UserPoint initializeUserPoint(Long userId) {
        log.info("사용자 포인트 초기화. userId: {}", userId);

        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        // 이미 초기화되어 있으면 기존 데이터 반환
        if (userPointRepository.existsByUserId(userId)) {
            return getUserPoint(userId);
        }

        UserPoint userPoint = UserPoint.create(userId);
        return userPointRepository.save(userPoint);
    }

    /**
     * 사용자 포인트 조회
     */
    public UserPoint getUserPoint(Long userId) {
        return userPointRepository.findByUserId(userId)
            .orElseThrow(() -> new UserPointNotFoundException(userId));
    }

    /**
     * 사용자 포인트 조회 또는 초기화
     */
    @Transactional
    public UserPoint getOrInitializeUserPoint(Long userId) {
        return userPointRepository.findByUserId(userId)
            .orElseGet(() -> initializeUserPoint(userId));
    }

    /**
     * 포인트 적립
     */
    @Transactional
    public PointTransaction earnPoints(Long userId, PointTransactionType type,
                                      Integer points, Long referenceId, String referenceType) {
        log.info("포인트 적립. userId: {}, type: {}, points: {}", userId, type, points);

        if (!type.isEarn()) {
            throw new IllegalArgumentException("적립 타입이 아닙니다: " + type);
        }

        UserPoint userPoint = getOrInitializeUserPoint(userId);

        // 기본 포인트가 0이면 파라미터 포인트 사용, 아니면 기본 포인트 사용
        int earnPoints = (type.getDefaultPoints() == 0) ? points : type.getDefaultPoints();

        try {
            userPoint.earnPoints(earnPoints);
        } catch (IllegalStateException e) {
            throw new DailyPointLimitExceededException(e.getMessage());
        }

        userPointRepository.save(userPoint);

        // 거래 내역 생성
        PointTransaction transaction = PointTransaction.earn(
            userId, type, earnPoints, userPoint.getAvailablePoints(),
            referenceId, referenceType
        );

        return transactionRepository.save(transaction);
    }

    /**
     * 포인트 차감
     */
    @Transactional
    public PointTransaction deductPoints(Long userId, PointTransactionType type,
                                        Integer points, Long referenceId, String referenceType) {
        log.info("포인트 차감. userId: {}, type: {}, points: {}", userId, type, points);

        if (type.isEarn()) {
            throw new IllegalArgumentException("차감 타입이 아닙니다: " + type);
        }

        UserPoint userPoint = getUserPoint(userId);

        int deductPoints = Math.abs(type.getDefaultPoints() == 0 ? points : type.getDefaultPoints());

        try {
            userPoint.deductPoints(deductPoints);
        } catch (IllegalStateException e) {
            throw new InsufficientPointsException(e.getMessage());
        }

        userPointRepository.save(userPoint);

        // 거래 내역 생성
        PointTransaction transaction = PointTransaction.deduct(
            userId, type, deductPoints, userPoint.getAvailablePoints(),
            referenceId, referenceType
        );

        return transactionRepository.save(transaction);
    }

    /**
     * 포인트 사용
     */
    @Transactional
    public PointTransaction usePoints(Long userId, Integer points, String description) {
        log.info("포인트 사용. userId: {}, points: {}", userId, points);

        UserPoint userPoint = getUserPoint(userId);

        try {
            userPoint.usePoints(points);
        } catch (IllegalStateException e) {
            throw new InsufficientPointsException(e.getMessage());
        }

        userPointRepository.save(userPoint);

        // 거래 내역 생성
        PointTransaction transaction = PointTransaction.use(
            userId, points, userPoint.getAvailablePoints(), description
        );

        return transactionRepository.save(transaction);
    }

    /**
     * 관리자 포인트 지급/차감
     */
    @Transactional
    public PointTransaction adminAdjustPoints(Long userId, Long adminId,
                                             Integer points, String description) {
        log.info("관리자 포인트 조정. userId: {}, adminId: {}, points: {}", userId, adminId, points);

        UserPoint userPoint = getOrInitializeUserPoint(userId);

        if (points > 0) {
            userPoint.adminGrantPoints(points);
        } else if (points < 0) {
            userPoint.deductPoints(Math.abs(points));
        } else {
            throw new IllegalArgumentException("포인트는 0이 아니어야 합니다.");
        }

        userPointRepository.save(userPoint);

        // 거래 내역 생성
        PointTransaction transaction = PointTransaction.admin(
            userId, adminId, points, userPoint.getAvailablePoints(), description
        );

        return transactionRepository.save(transaction);
    }

    /**
     * 사용자 거래 내역 조회
     */
    public Page<PointTransaction> getUserTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 사용자 특정 기간 거래 내역 조회
     */
    public Page<PointTransaction> getUserTransactionsByPeriod(Long userId,
                                                              LocalDateTime startDate,
                                                              LocalDateTime endDate,
                                                              Pageable pageable) {
        return transactionRepository.findByUserIdAndPeriod(userId, startDate, endDate, pageable);
    }

    /**
     * 포인트 랭킹 조회
     */
    public Page<UserPoint> getPointRanking(Pageable pageable) {
        return userPointRepository.findAllByOrderByTotalPointsDesc(pageable);
    }

    /**
     * 레벨별 사용자 수 조회
     */
    public long countUsersByLevel(UserLevel level) {
        return userPointRepository.countByCurrentLevel(level);
    }

    /**
     * 특정 레벨 이상 사용자 조회
     */
    public Page<UserPoint> getUsersByLevelOrHigher(UserLevel level, Pageable pageable) {
        return userPointRepository.findByLevelGreaterThanEqual(level, pageable);
    }

    /**
     * 전체 포인트 통계
     */
    public PointStatistics getPointStatistics() {
        Long totalPoints = userPointRepository.sumTotalPoints();
        Double avgPoints = userPointRepository.averageTotalPoints();
        long totalUsers = userPointRepository.count();

        return new PointStatistics(
            totalPoints != null ? totalPoints : 0L,
            avgPoints != null ? avgPoints : 0.0,
            totalUsers
        );
    }

    /**
     * 포인트 통계 내부 클래스
     */
    public record PointStatistics(Long totalPoints, Double averagePoints, Long totalUsers) {}
}
