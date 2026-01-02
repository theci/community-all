package com.community.platform.content.infrastructure.persistence;

import com.community.platform.content.domain.Comment;
import com.community.platform.content.domain.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글의 댓글 목록 조회 (댓글 섹션용)
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.status = :status ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdAndStatusOrderByCreatedAt(@Param("postId") Long postId, 
                                                        @Param("status") CommentStatus status);
    
    // 특정 게시글의 최상위 댓글만 조회 (대댓글 제외)
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parentComment IS NULL AND c.status = :status ORDER BY c.createdAt ASC")
    List<Comment> findRootCommentsByPostIdAndStatus(@Param("postId") Long postId, 
                                                    @Param("status") CommentStatus status);
    
    // 특정 댓글의 대댓글 목록 조회 (대댓글 표시용)
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentCommentId AND c.status = :status ORDER BY c.createdAt ASC")
    List<Comment> findByParentCommentIdAndStatusOrderByCreatedAt(@Param("parentCommentId") Long parentCommentId, 
                                                                @Param("status") CommentStatus status);
    
    // 특정 사용자가 작성한 댓글 조회 (마이페이지용)
    Page<Comment> findByAuthorIdAndStatusOrderByCreatedAtDesc(Long authorId, CommentStatus status, Pageable pageable);
    
    // 특정 게시글의 댓글 개수 조회 (게시글 목록에서 댓글 수 표시)
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.status = :status")
    Long countByPostIdAndStatus(@Param("postId") Long postId, @Param("status") CommentStatus status);
    
    // 특정 사용자의 댓글 개수 조회 (프로필 페이지용)
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.authorId = :authorId AND c.status = :status")
    Long countByAuthorIdAndStatus(@Param("authorId") Long authorId, @Param("status") CommentStatus status);
    
    // 댓글 좋아요 수 증가 (댓글 좋아요 시 호출)
    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    void incrementLikeCount(@Param("commentId") Long commentId);
    
    // 댓글 좋아요 수 감소 (댓글 좋아요 취소 시 호출)
    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :commentId AND c.likeCount > 0")
    void decrementLikeCount(@Param("commentId") Long commentId);
    
    // 특정 게시글의 모든 댓글 삭제 (게시글 삭제 시 호출)
    @Modifying
    @Query("UPDATE Comment c SET c.status = :status WHERE c.post.id = :postId")
    void updateStatusByPostId(@Param("postId") Long postId, @Param("status") CommentStatus status);
    
    // 특정 댓글의 모든 대댓글 삭제 (부모 댓글 삭제 시 호출)
    @Modifying
    @Query("UPDATE Comment c SET c.status = :status WHERE c.parentComment.id = :parentCommentId")
    void updateStatusByParentCommentId(@Param("parentCommentId") Long parentCommentId, 
                                      @Param("status") CommentStatus status);
    
    // 내용에 특정 키워드가 포함된 댓글 검색 (관리자의 댓글 관리용)
    @Query("SELECT c FROM Comment c WHERE c.content LIKE %:keyword% AND c.status = :status ORDER BY c.createdAt DESC")
    Page<Comment> findByContentContainingAndStatus(@Param("keyword") String keyword, 
                                                   @Param("status") CommentStatus status, 
                                                   Pageable pageable);
    
    // 특정 게시글의 계층형 댓글 구조 조회 (트리 구조로 댓글 표시)
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.status = :status ORDER BY " +
           "CASE WHEN c.parentComment IS NULL THEN c.id ELSE c.parentComment.id END, " +
           "c.parentComment.id ASC NULLS FIRST, c.createdAt ASC")
    List<Comment> findByPostIdWithHierarchy(@Param("postId") Long postId, 
                                           @Param("status") CommentStatus status);
    
    // 최근 댓글 조회 (관리자 대시보드용)
    @Query("SELECT c FROM Comment c WHERE c.status = :status ORDER BY c.createdAt DESC")
    Page<Comment> findRecentCommentsByStatus(@Param("status") CommentStatus status, Pageable pageable);
}