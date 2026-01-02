package com.community.platform.content.infrastructure.persistence;

import com.community.platform.content.domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    // 태그명으로 태그 조회 (기존 태그 검색용)
    Optional<Tag> findByName(String name);
    
    // 태그명 존재 여부 확인 (태그 중복 체크용)
    boolean existsByName(String name);
    
    // 사용 횟수가 많은 인기 태그 조회 (태그 클라우드용)
    @Query("SELECT t FROM Tag t ORDER BY t.usageCount DESC")
    Page<Tag> findPopularTags(Pageable pageable);
    
    // 사용 횟수 기준 상위 N개 태그 조회 (인기 태그 섹션용)
    @Query("SELECT t FROM Tag t WHERE t.usageCount > 0 ORDER BY t.usageCount DESC")
    List<Tag> findTopUsedTags(Pageable pageable);
    
    // 태그명에 키워드가 포함된 태그 검색 (태그 자동완성용)
    @Query("SELECT t FROM Tag t WHERE t.name LIKE %:keyword% ORDER BY t.usageCount DESC")
    List<Tag> findByNameContainingOrderByUsageCountDesc(@Param("keyword") String keyword);
    
    // 사용되지 않은 태그 조회 (관리자의 태그 정리용)
    @Query("SELECT t FROM Tag t WHERE t.usageCount = 0")
    List<Tag> findUnusedTags();
    
    // 특정 사용 횟수 이상인 태그 조회 (활성 태그 필터링용)
    @Query("SELECT t FROM Tag t WHERE t.usageCount >= :minUsageCount ORDER BY t.usageCount DESC")
    List<Tag> findByUsageCountGreaterThanEqual(@Param("minUsageCount") Long minUsageCount);
    
    // 특정 색상의 태그 조회 (색상별 태그 그룹핑용)
    List<Tag> findByColor(String color);
    
    // 최근 생성된 태그 조회 (새로운 태그 모니터링용)
    @Query("SELECT t FROM Tag t ORDER BY t.createdAt DESC")
    Page<Tag> findRecentlyCreatedTags(Pageable pageable);
    
    // 태그명 리스트로 태그들 조회 (게시글 태그 처리용)
    @Query("SELECT t FROM Tag t WHERE t.name IN :tagNames")
    List<Tag> findByNameIn(@Param("tagNames") List<String> tagNames);
    
    // 전체 태그 수 조회 (통계용)
    @Query("SELECT COUNT(t) FROM Tag t")
    Long countAllTags();
    
    // 활성 태그 수 조회 (사용 중인 태그 통계용)
    @Query("SELECT COUNT(t) FROM Tag t WHERE t.usageCount > 0")
    Long countActiveTags();
    
    // 태그별 게시글 수와 함께 조회 (태그 통계용)
    @Query("SELECT t, COUNT(pt) as postCount FROM Tag t " +
           "LEFT JOIN PostTag pt ON pt.tag.id = t.id " +
           "GROUP BY t " +
           "ORDER BY COUNT(pt) DESC")
    List<Object[]> findTagsWithPostCount();
    
    // 특정 게시글에 연결된 태그들 조회 (게시글 상세 페이지용)
    @Query("SELECT t FROM Tag t JOIN PostTag pt ON pt.tag.id = t.id WHERE pt.post.id = :postId")
    List<Tag> findByPostId(@Param("postId") Long postId);
}