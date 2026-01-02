package com.community.platform.content.infrastructure.persistence;

import com.community.platform.content.domain.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    // 특정 게시글의 모든 태그 연결 조회 (게시글의 태그 목록용)
    @Query("SELECT pt FROM PostTag pt JOIN FETCH pt.tag WHERE pt.post.id = :postId")
    List<PostTag> findByPostIdWithTag(@Param("postId") Long postId);
    
    // 특정 태그가 연결된 모든 게시글 연결 조회 (태그별 게시글 목록용)
    @Query("SELECT pt FROM PostTag pt JOIN FETCH pt.post WHERE pt.tag.id = :tagId")
    List<PostTag> findByTagIdWithPost(@Param("tagId") Long tagId);
    
    // 특정 게시글과 태그의 연결 조회 (중복 체크용)
    Optional<PostTag> findByPostIdAndTagId(Long postId, Long tagId);
    
    // 특정 게시글과 태그의 연결 존재 여부 확인 (중복 체크용)
    boolean existsByPostIdAndTagId(Long postId, Long tagId);
    
    // 특정 게시글의 태그 개수 조회 (게시글당 태그 수 제한용)
    @Query("SELECT COUNT(pt) FROM PostTag pt WHERE pt.post.id = :postId")
    Long countByPostId(@Param("postId") Long postId);
    
    // 특정 태그가 연결된 게시글 개수 조회 (태그 인기도 측정용)
    @Query("SELECT COUNT(pt) FROM PostTag pt WHERE pt.tag.id = :tagId")
    Long countByTagId(@Param("tagId") Long tagId);
    
    // 특정 게시글의 모든 태그 연결 삭제 (게시글 수정 시 기존 태그 제거용)
    @Modifying
    @Query("DELETE FROM PostTag pt WHERE pt.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
    
    // 특정 태그의 모든 게시글 연결 삭제 (태그 삭제 시 연결 정리용)
    @Modifying
    @Query("DELETE FROM PostTag pt WHERE pt.tag.id = :tagId")
    void deleteByTagId(@Param("tagId") Long tagId);
    
    // 특정 게시글과 태그의 연결 삭제 (개별 태그 제거용)
    @Modifying
    @Query("DELETE FROM PostTag pt WHERE pt.post.id = :postId AND pt.tag.id = :tagId")
    void deleteByPostIdAndTagId(@Param("postId") Long postId, @Param("tagId") Long tagId);
    
    // 게시글별 태그 이름 목록 조회 (API 응답용)
    @Query("SELECT pt.post.id, t.name FROM PostTag pt JOIN pt.tag t WHERE pt.post.id IN :postIds")
    List<Object[]> findTagNamesByPostIds(@Param("postIds") List<Long> postIds);
    
    // 태그별 최근 게시글 연결 조회 (태그 활동성 측정용)
    @Query("SELECT pt FROM PostTag pt WHERE pt.tag.id = :tagId ORDER BY pt.createdAt DESC")
    List<PostTag> findByTagIdOrderByCreatedAtDesc(@Param("tagId") Long tagId);
    
    // 사용자가 자주 사용하는 태그 조회 (게시글 작성 시 태그 추천용)
    @Query("SELECT t.id, t.name, COUNT(pt) as usage FROM PostTag pt " +
           "JOIN pt.tag t " +
           "JOIN pt.post p " +
           "WHERE p.authorId = :authorId " +
           "GROUP BY t.id, t.name " +
           "ORDER BY COUNT(pt) DESC")
    List<Object[]> findFrequentTagsByAuthorId(@Param("authorId") Long authorId);
    
    // 연관 태그 조회 (함께 많이 사용되는 태그들)
    @Query("SELECT t2.id, t2.name, COUNT(*) as cooccurrence FROM PostTag pt1 " +
           "JOIN PostTag pt2 ON pt1.post.id = pt2.post.id " +
           "JOIN pt2.tag t2 " +
           "WHERE pt1.tag.id = :tagId AND pt2.tag.id != :tagId " +
           "GROUP BY t2.id, t2.name " +
           "ORDER BY COUNT(*) DESC")
    List<Object[]> findRelatedTags(@Param("tagId") Long tagId);
}