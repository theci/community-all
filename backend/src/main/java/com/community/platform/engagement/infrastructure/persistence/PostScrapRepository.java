package com.community.platform.engagement.infrastructure.persistence;

import com.community.platform.engagement.domain.PostScrap;
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
public interface PostScrapRepository extends JpaRepository<PostScrap, Long> {

    // 특정 사용자와 게시글의 스크랩 조회 (스크랩 상태 확인용)
    Optional<PostScrap> findByUserIdAndPostId(Long userId, Long postId);
    
    // 특정 사용자와 게시글의 스크랩 존재 여부 확인 (스크랩 중복 체크용)
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    
    // 특정 사용자의 모든 스크랩 조회 (마이페이지 스크랩 목록용)
    Page<PostScrap> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 특정 스크랩 폴더의 스크랩 목록 조회 (폴더별 스크랩 관리용)
    Page<PostScrap> findByScrapFolderIdOrderByCreatedAtDesc(Long scrapFolderId, Pageable pageable);
    
    // 특정 사용자의 특정 폴더 스크랩 조회 (사용자의 폴더별 스크랩 목록용)
    @Query("SELECT ps FROM PostScrap ps LEFT JOIN FETCH ps.scrapFolder WHERE ps.userId = :userId AND ps.scrapFolder.id = :scrapFolderId ORDER BY ps.createdAt DESC")
    Page<PostScrap> findByUserIdAndScrapFolderIdOrderByCreatedAtDesc(@Param("userId") Long userId,
                                                                    @Param("scrapFolderId") Long scrapFolderId,
                                                                    Pageable pageable);
    
    // 특정 게시글의 스크랩 개수 조회 (게시글 인기도 측정용)
    @Query("SELECT COUNT(ps) FROM PostScrap ps WHERE ps.postId = :postId")
    Long countByPostId(@Param("postId") Long postId);
    
    // 특정 사용자의 스크랩 개수 조회 (사용자 활동 통계용)
    @Query("SELECT COUNT(ps) FROM PostScrap ps WHERE ps.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    // 특정 폴더의 스크랩 개수 조회 (폴더 관리용)
    @Query("SELECT COUNT(ps) FROM PostScrap ps WHERE ps.scrapFolder.id = :scrapFolderId")
    Long countByScrapFolderId(@Param("scrapFolderId") Long scrapFolderId);
    
    // 최근 스크랩 활동 조회 (실시간 활동 피드용)
    @Query("SELECT ps FROM PostScrap ps ORDER BY ps.createdAt DESC")
    Page<PostScrap> findRecentScraps(Pageable pageable);
    
    // 특정 기간 동안의 스크랩 개수 조회 (스크랩 트렌드 분석용)
    @Query("SELECT COUNT(ps) FROM PostScrap ps WHERE ps.postId = :postId AND ps.createdAt BETWEEN :startDate AND :endDate")
    Long countByPostIdAndCreatedAtBetween(@Param("postId") Long postId, 
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    // 가장 많이 스크랩된 게시글 조회 (인기 게시글 분석용)
    @Query("SELECT ps.postId, COUNT(ps) as scrapCount FROM PostScrap ps " +
           "WHERE ps.createdAt >= :fromDate " +
           "GROUP BY ps.postId " +
           "ORDER BY COUNT(ps) DESC")
    List<Object[]> findMostScrappedPostIds(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);
    
    // 사용자의 스크랩 삭제 (스크랩 해제 시 사용)
    void deleteByUserIdAndPostId(Long userId, Long postId);
    
    // 특정 게시글의 모든 스크랩 삭제 (게시글 삭제 시 사용)
    void deleteByPostId(Long postId);
    
    // 특정 사용자의 모든 스크랩 삭제 (회원 탈퇴 시 사용)
    void deleteByUserId(Long userId);
    
    // 특정 폴더의 모든 스크랩 삭제 (폴더 삭제 시 사용)
    void deleteByScrapFolderId(Long scrapFolderId);
    
    // 스크랩 폴더 변경 (스크랩을 다른 폴더로 이동)
    @Query("SELECT ps FROM PostScrap ps WHERE ps.userId = :userId AND ps.postId IN :postIds")
    List<PostScrap> findByUserIdAndPostIdIn(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);
}