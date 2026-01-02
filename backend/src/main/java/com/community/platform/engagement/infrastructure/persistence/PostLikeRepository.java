package com.community.platform.engagement.infrastructure.persistence;

import com.community.platform.engagement.domain.PostLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // 특정 사용자와 게시글의 좋아요 조회 (좋아요 상태 확인용)
    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);
    
    // 특정 사용자와 게시글의 좋아요 존재 여부 확인 (좋아요 중복 체크용)
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    
    // 특정 게시글의 좋아요 개수 조회 (게시글 상세 페이지용)
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.postId = :postId")
    Long countByPostId(@Param("postId") Long postId);
    
    // 특정 사용자가 좋아요한 게시글 목록 조회 (마이페이지 좋아요 목록용)
    Page<PostLike> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 특정 게시글을 좋아요한 사용자 목록 조회 (게시글 좋아요 사용자 목록용)
    Page<PostLike> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);
    
    // 특정 기간 동안의 좋아요 개수 조회 (게시글 인기도 분석용)
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.postId = :postId AND pl.createdAt BETWEEN :startDate AND :endDate")
    Long countByPostIdAndCreatedAtBetween(@Param("postId") Long postId, 
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    // 사용자별 좋아요 개수 조회 (사용자 활동 통계용)
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    // 최근 좋아요 활동 조회 (실시간 활동 피드용)
    @Query("SELECT pl FROM PostLike pl ORDER BY pl.createdAt DESC")
    Page<PostLike> findRecentLikes(Pageable pageable);
    
    // 특정 사용자들의 게시글에 대한 좋아요 조회 (팔로잉 사용자 게시글 좋아요 현황)
    @Query("SELECT pl FROM PostLike pl WHERE pl.userId = :userId AND pl.postId IN :postIds")
    List<PostLike> findByUserIdAndPostIdIn(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);
    
    // 가장 많은 좋아요를 받은 게시글 ID 조회 (인기 게시글 분석용)
    @Query("SELECT pl.postId, COUNT(pl) as likeCount FROM PostLike pl " +
           "WHERE pl.createdAt >= :fromDate " +
           "GROUP BY pl.postId " +
           "ORDER BY COUNT(pl) DESC")
    List<Object[]> findMostLikedPostIds(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);
    
    // 특정 기간 동안 좋아요가 가장 활발한 시간대 분석 (좋아요 패턴 분석용)
    @Query("SELECT HOUR(pl.createdAt) as hour, COUNT(pl) as count FROM PostLike pl " +
           "WHERE pl.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY HOUR(pl.createdAt) " +
           "ORDER BY COUNT(pl) DESC")
    List<Object[]> findLikeActivityByHour(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    // 사용자의 좋아요 취소 (좋아요 해제 시 사용)
    void deleteByUserIdAndPostId(Long userId, Long postId);
    
    // 특정 게시글의 모든 좋아요 삭제 (게시글 삭제 시 사용)
    void deleteByPostId(Long postId);
    
    // 특정 사용자의 모든 좋아요 삭제 (회원 탈퇴 시 사용)
    void deleteByUserId(Long userId);
}