package com.community.platform.moderation.infrastructure.persistence;

import com.community.platform.moderation.domain.PenaltyType;
import com.community.platform.moderation.domain.UserPenalty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 제재 Repository
 */
public interface UserPenaltyRepository extends JpaRepository<UserPenalty, Long> {

    /**
     * 사용자의 활성화된 제재 목록 조회
     */
    @Query("SELECT p FROM UserPenalty p WHERE p.userId = :userId " +
           "AND p.isActive = true " +
           "AND (p.endDate IS NULL OR p.endDate > CURRENT_TIMESTAMP) " +
           "ORDER BY p.createdAt DESC")
    List<UserPenalty> findActiveByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 특정 타입 활성 제재 조회
     */
    @Query("SELECT p FROM UserPenalty p WHERE p.userId = :userId " +
           "AND p.penaltyType IN :types " +
           "AND p.isActive = true " +
           "AND (p.endDate IS NULL OR p.endDate > CURRENT_TIMESTAMP)")
    List<UserPenalty> findActiveByUserIdAndTypes(@Param("userId") Long userId,
                                                  @Param("types") List<PenaltyType> types);

    /**
     * 만료된 제재 조회 (스케줄러용)
     */
    @Query("SELECT p FROM UserPenalty p WHERE p.isActive = true " +
           "AND p.endDate IS NOT NULL " +
           "AND p.endDate < CURRENT_TIMESTAMP")
    List<UserPenalty> findExpiredPenalties();

    /**
     * 사용자의 모든 제재 이력 조회 (페이징)
     */
    Page<UserPenalty> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 활성화된 모든 제재 목록 조회 (관리자용)
     */
    @Query("SELECT p FROM UserPenalty p WHERE p.isActive = true " +
           "AND (p.endDate IS NULL OR p.endDate > CURRENT_TIMESTAMP) " +
           "ORDER BY p.createdAt DESC")
    Page<UserPenalty> findActivePenalties(Pageable pageable);

    /**
     * 특정 타입의 활성 제재 수 조회
     */
    @Query("SELECT COUNT(p) FROM UserPenalty p WHERE p.userId = :userId " +
           "AND p.penaltyType = :type " +
           "AND p.isActive = true " +
           "AND (p.endDate IS NULL OR p.endDate > CURRENT_TIMESTAMP)")
    Long countActiveByUserIdAndType(@Param("userId") Long userId, @Param("type") PenaltyType type);

    /**
     * 사용자의 제재 횟수 조회 (기간 내)
     */
    @Query("SELECT COUNT(p) FROM UserPenalty p WHERE p.userId = :userId " +
           "AND p.createdAt > :since")
    Long countByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
