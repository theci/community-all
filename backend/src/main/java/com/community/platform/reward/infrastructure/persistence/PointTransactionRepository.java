package com.community.platform.reward.infrastructure.persistence;

import com.community.platform.reward.domain.PointTransaction;
import com.community.platform.reward.domain.PointTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 포인트 거래 내역 Repository
 */
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    /**
     * 사용자별 거래 내역 조회 (최신순)
     */
    Page<PointTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자별 특정 기간 거래 내역
     */
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.userId = :userId " +
           "AND pt.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY pt.createdAt DESC")
    Page<PointTransaction> findByUserIdAndPeriod(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);

    /**
     * 사용자별 특정 거래 유형 내역
     */
    Page<PointTransaction> findByUserIdAndTransactionType(
        Long userId, PointTransactionType type, Pageable pageable);

    /**
     * 사용자의 총 획득 포인트
     */
    @Query("SELECT COALESCE(SUM(pt.points), 0) FROM PointTransaction pt " +
           "WHERE pt.userId = :userId AND pt.points > 0")
    Integer sumEarnedPointsByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 총 차감 포인트
     */
    @Query("SELECT COALESCE(SUM(pt.points), 0) FROM PointTransaction pt " +
           "WHERE pt.userId = :userId AND pt.points < 0")
    Integer sumDeductedPointsByUserId(@Param("userId") Long userId);

    /**
     * 특정 참조 엔티티 관련 거래 내역
     */
    List<PointTransaction> findByReferenceIdAndReferenceType(Long referenceId, String referenceType);

    /**
     * 거래 유형별 통계
     */
    @Query("SELECT pt.transactionType, COUNT(pt), SUM(pt.points) FROM PointTransaction pt " +
           "WHERE pt.createdAt >= :startDate GROUP BY pt.transactionType")
    List<Object[]> getTransactionStatistics(@Param("startDate") LocalDateTime startDate);

    /**
     * 최근 거래 내역 조회 (관리자용)
     */
    Page<PointTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 관리자가 처리한 거래 내역
     */
    Page<PointTransaction> findByAdminIdIsNotNullOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 특정 기간 동안의 전체 포인트 이동량
     */
    @Query("SELECT SUM(pt.points) FROM PointTransaction pt " +
           "WHERE pt.createdAt BETWEEN :startDate AND :endDate")
    Long sumPointsByPeriod(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * 전체 시스템 포인트 합계 (관리자 통계용)
     */
    @Query("SELECT COALESCE(SUM(pt.points), 0) FROM PointTransaction pt WHERE pt.points > 0")
    Long getTotalPoints();
}
