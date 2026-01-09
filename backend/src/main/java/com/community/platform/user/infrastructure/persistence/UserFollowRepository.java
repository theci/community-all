package com.community.platform.user.infrastructure.persistence;

import com.community.platform.user.domain.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    /**
     * 팔로우 관계 존재 여부 확인
     */
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /**
     * 팔로우 관계 조회
     */
    Optional<UserFollow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /**
     * 특정 사용자가 팔로우하는 사용자 ID 목록
     */
    @Query("SELECT uf.followingId FROM UserFollow uf WHERE uf.followerId = :followerId")
    List<Long> findFollowingIdsByFollowerId(@Param("followerId") Long followerId);

    /**
     * 특정 사용자를 팔로우하는 사용자 ID 목록
     */
    @Query("SELECT uf.followerId FROM UserFollow uf WHERE uf.followingId = :followingId")
    List<Long> findFollowerIdsByFollowingId(@Param("followingId") Long followingId);

    /**
     * 팔로잉 수 카운트
     */
    long countByFollowerId(Long followerId);

    /**
     * 팔로워 수 카운트
     */
    long countByFollowingId(Long followingId);

    /**
     * 팔로우 삭제
     */
    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
