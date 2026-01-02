package com.community.platform.content.infrastructure.persistence;

import com.community.platform.content.domain.Post;
import com.community.platform.content.domain.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    // 발행된 게시글만 최신순으로 조회 (메인 페이지용)
    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.status = :status ORDER BY p.publishedAt DESC")
    Page<Post> findByStatusOrderByPublishedAtDesc(@Param("status") PostStatus status, Pageable pageable);

    // 특정 작성자의 게시글 조회 (마이페이지용)
    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.authorId = :authorId AND p.status = :status ORDER BY p.createdAt DESC")
    Page<Post> findByAuthorIdAndStatusOrderByCreatedAtDesc(@Param("authorId") Long authorId, @Param("status") PostStatus status, Pageable pageable);

    // 카테고리별 게시글 조회 (카테고리 페이지용)
    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.category.id = :categoryId AND p.status = :status ORDER BY p.publishedAt DESC")
    Page<Post> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId,
                                        @Param("status") PostStatus status,
                                        Pageable pageable);

    // 공지사항 조회 (공지사항은 상단 고정)
    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.isNoticePost = true AND p.status = :status ORDER BY p.publishedAt DESC")
    List<Post> findNoticePostsByStatus(@Param("status") PostStatus status);

    // 제목과 내용에서 키워드 검색 (게시글 검색 기능)
    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) AND p.status = :status ORDER BY p.publishedAt DESC")
    Page<Post> searchByKeywordAndStatus(@Param("keyword") String keyword,
                                       @Param("status") PostStatus status,
                                       Pageable pageable);

    // 좋아요 수가 많은 인기 게시글 조회 (인기글 페이지용)
    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.status = :status AND p.publishedAt >= :fromDate ORDER BY p.likeCount DESC, p.publishedAt DESC")
    Page<Post> findPopularPostsByPeriod(@Param("status") PostStatus status,
                                       @Param("fromDate") LocalDateTime fromDate,
                                       Pageable pageable);
    
    // 특정 기간 동안 발행된 게시글 조회 (통계용)
    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.status = :status AND p.publishedAt BETWEEN :startDate AND :endDate")
    List<Post> findByStatusAndPublishedAtBetween(@Param("status") PostStatus status,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
    
    // 게시글 조회수 증가 (조회할 때마다 호출)
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);
    
    // 게시글 좋아요 수 증가 (좋아요 추가 시 호출)
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void incrementLikeCount(@Param("postId") Long postId);
    
    // 게시글 좋아요 수 감소 (좋아요 취소 시 호출)
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :postId AND p.likeCount > 0")
    void decrementLikeCount(@Param("postId") Long postId);
    
    // 게시글 댓글 수 증가 (댓글 추가 시 호출)
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    void incrementCommentCount(@Param("postId") Long postId);
    
    // 게시글 댓글 수 감소 (댓글 삭제 시 호출)
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :postId AND p.commentCount > 0")
    void decrementCommentCount(@Param("postId") Long postId);
    
    // 작성자별 게시글 개수 조회 (프로필 페이지용)
    @Query("SELECT COUNT(p) FROM Post p WHERE p.authorId = :authorId AND p.status = :status")
    Long countByAuthorIdAndStatus(@Param("authorId") Long authorId, @Param("status") PostStatus status);
    
    // 카테고리별 게시글 개수 조회 (카테고리 통계용)
    @Query("SELECT COUNT(p) FROM Post p WHERE p.category.id = :categoryId AND p.status = :status")
    Long countByCategoryIdAndStatus(@Param("categoryId") Long categoryId, @Param("status") PostStatus status);
    
    // 태그가 포함된 게시글 조회 (태그별 게시글 목록)
    @Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.category JOIN p.postTags pt WHERE pt.tag.id = :tagId AND p.status = :status ORDER BY p.publishedAt DESC")
    Page<Post> findByTagIdAndStatus(@Param("tagId") Long tagId,
                                   @Param("status") PostStatus status,
                                   Pageable pageable);
    
    // ID와 상태로 게시글 조회 (상세 페이지에서 유효성 체크)
    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.id = :id AND p.status = :status")
    Optional<Post> findByIdAndStatus(@Param("id") Long id, @Param("status") PostStatus status);
    
    // 관련 정보와 함께 게시글 조회 (N+1 문제 방지)
    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.id = :postId")
    Optional<Post> findByIdWithCategory(@Param("postId") Long postId);
}