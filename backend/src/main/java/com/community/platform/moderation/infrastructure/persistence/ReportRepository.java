package com.community.platform.moderation.infrastructure.persistence;

import com.community.platform.moderation.domain.Report;
import com.community.platform.moderation.domain.ReportStatus;
import com.community.platform.moderation.domain.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 신고 리포지토리
 */
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * 신고자와 대상으로 신고 조회 (중복 신고 체크)
     */
    @Query("SELECT r FROM Report r WHERE r.reporterId = :reporterId " +
           "AND r.targetType = :targetType AND r.targetId = :targetId " +
           "AND r.status != 'REJECTED'")
    Optional<Report> findByReporterAndTarget(
        @Param("reporterId") Long reporterId,
        @Param("targetType") ReportTargetType targetType,
        @Param("targetId") Long targetId
    );

    /**
     * 상태별 신고 목록 조회 (페이징)
     */
    Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    /**
     * 특정 대상에 대한 모든 신고 조회
     */
    @Query("SELECT r FROM Report r WHERE r.targetType = :targetType " +
           "AND r.targetId = :targetId ORDER BY r.createdAt DESC")
    List<Report> findByTarget(
        @Param("targetType") ReportTargetType targetType,
        @Param("targetId") Long targetId
    );

    /**
     * 신고자별 신고 목록 조회
     */
    Page<Report> findByReporterIdOrderByCreatedAtDesc(Long reporterId, Pageable pageable);

    /**
     * 신고 대상 사용자별 신고 목록 조회
     */
    Page<Report> findByReportedUserIdOrderByCreatedAtDesc(Long reportedUserId, Pageable pageable);

    /**
     * 검토자별 처리한 신고 목록 조회
     */
    Page<Report> findByReviewerIdOrderByReviewedAtDesc(Long reviewerId, Pageable pageable);

    /**
     * 대상 유형별 신고 목록 조회
     */
    Page<Report> findByTargetTypeOrderByCreatedAtDesc(ReportTargetType targetType, Pageable pageable);

    /**
     * 기간별 신고 통계
     */
    @Query("SELECT COUNT(r) FROM Report r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate,
                         @Param("endDate") LocalDateTime endDate);

    /**
     * 상태별 신고 수 조회
     */
    Long countByStatus(ReportStatus status);

    /**
     * 특정 대상의 신고 수 조회
     */
    @Query("SELECT COUNT(r) FROM Report r WHERE r.targetType = :targetType " +
           "AND r.targetId = :targetId AND r.status != 'REJECTED'")
    Long countByTarget(
        @Param("targetType") ReportTargetType targetType,
        @Param("targetId") Long targetId
    );

    /**
     * 검토 대기 중인 신고 조회 (오래된 순)
     */
    @Query("SELECT r FROM Report r WHERE r.status = 'PENDING' " +
           "ORDER BY r.createdAt ASC")
    Page<Report> findPendingReports(Pageable pageable);

    /**
     * 고위험 신고 조회 (심각도가 높은 신고)
     */
    @Query("SELECT r FROM Report r WHERE r.status = 'PENDING' " +
           "AND r.reason IN ('SEXUAL', 'VIOLENCE', 'HATE_SPEECH', 'ILLEGAL_CONTENT', 'PERSONAL_INFO') " +
           "ORDER BY r.createdAt ASC")
    Page<Report> findHighSeverityReports(Pageable pageable);

    /**
     * 특정 사용자에 대한 승인된 신고 수 조회 (기간 내)
     * 자동 제재 로직에서 사용
     */
    @Query("SELECT COUNT(r) FROM Report r WHERE r.reportedUserId = :userId " +
           "AND r.status = 'APPROVED' AND r.reviewedAt > :since")
    Long countApprovedReportsByUser(@Param("userId") Long userId,
                                    @Param("since") LocalDateTime since);
}
