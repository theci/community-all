package com.community.platform.reward.infrastructure.persistence;

import com.community.platform.reward.domain.UserPoint;
import com.community.platform.reward.domain.UserLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

/**
 * 사용자 포인트 Repository
 */
public interface UserPointRepository extends JpaRepository<UserPoint, Long> {

    /**
     * 사용자 ID로 포인트 조회
     */
    Optional<UserPoint> findByUserId(Long userId);

    /**
     * 사용자 ID로 포인트 존재 여부 확인
     */
    boolean existsByUserId(Long userId);

    /**
     * 레벨별 사용자 수 조회
     */
    long countByCurrentLevel(UserLevel level);

    /**
     * 특정 레벨 이상의 사용자 조회
     */
    @Query("SELECT up FROM UserPoint up WHERE up.currentLevel >= :level ORDER BY up.totalPoints DESC")
    Page<UserPoint> findByLevelGreaterThanEqual(@Param("level") UserLevel level, Pageable pageable);

    /**
     * 포인트 랭킹 조회 (상위 N명)
     */
    Page<UserPoint> findAllByOrderByTotalPointsDesc(Pageable pageable);

    /**
     * 특정 포인트 이상 사용자 수
     */
    long countByTotalPointsGreaterThanEqual(Integer points);

    /**
     * 레벨별 사용자 목록 조회
     */
    Page<UserPoint> findByCurrentLevel(UserLevel level, Pageable pageable);

    /**
     * 사용 가능 포인트가 특정 값 이상인 사용자
     */
    List<UserPoint> findByAvailablePointsGreaterThanEqual(Integer points);

    /**
     * 총 포인트 통계
     */
    @Query("SELECT SUM(up.totalPoints) FROM UserPoint up")
    Long sumTotalPoints();

    /**
     * 평균 포인트
     */
    @Query("SELECT AVG(up.totalPoints) FROM UserPoint up")
    Double averageTotalPoints();
}
