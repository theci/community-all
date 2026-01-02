package com.community.platform.content.infrastructure.persistence;

import com.community.platform.content.domain.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 게시글 첨부파일 리포지토리
 */
@Repository
public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {

    /**
     * 특정 게시글의 모든 첨부파일 조회 (표시 순서대로)
     */
    @Query("SELECT a FROM PostAttachment a WHERE a.postId = :postId ORDER BY a.displayOrder ASC, a.id ASC")
    List<PostAttachment> findByPostIdOrderByDisplayOrder(@Param("postId") Long postId);

    /**
     * 특정 게시글의 첨부파일 개수 조회
     */
    @Query("SELECT COUNT(a) FROM PostAttachment a WHERE a.postId = :postId")
    Long countByPostId(@Param("postId") Long postId);

    /**
     * 여러 게시글의 첨부파일 일괄 조회 (IN 쿼리)
     */
    @Query("SELECT a FROM PostAttachment a WHERE a.postId IN :postIds ORDER BY a.postId, a.displayOrder ASC")
    List<PostAttachment> findByPostIdIn(@Param("postIds") List<Long> postIds);

    /**
     * 특정 게시글의 모든 첨부파일 삭제
     */
    void deleteByPostId(Long postId);
}
