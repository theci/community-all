package com.community.platform.content.infrastructure.persistence;

import com.community.platform.content.domain.Post;
import com.community.platform.content.domain.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 복잡한 쿼리를 위한 커스텀 Repository 인터페이스
 * QueryDSL을 사용한 동적 쿼리와 복합 검색 조건 처리
 */
public interface PostRepositoryCustom {

    // 복합 검색 조건으로 게시글 검색 (제목, 내용, 작성자, 카테고리, 태그 통합 검색)
    Page<Post> searchPostsWithFilters(String keyword,
                                     String searchType,
                                     Long categoryId,
                                     List<String> tagNames,
                                     PostStatus status,
                                     LocalDateTime startDate,
                                     LocalDateTime endDate,
                                     Pageable pageable);
    
    // 인기 게시글 조회 (조회수, 좋아요수, 댓글수를 종합한 점수 기반)
    Page<Post> findPopularPostsWithScore(PostStatus status, 
                                        LocalDateTime fromDate, 
                                        Pageable pageable);
    
    // 사용자 맞춤 게시글 추천 (사용자 관심사 기반)
    List<Post> findRecommendedPostsForUser(Long userId, 
                                          List<Long> followingUserIds, 
                                          List<String> interestedTags, 
                                          int limit);
    
    // 트렌딩 게시글 조회 (최근 활동이 많은 게시글)
    Page<Post> findTrendingPosts(PostStatus status, 
                                int hourRange, 
                                Pageable pageable);
    
    // 카테고리별 통계 조회 (카테고리별 게시글 수, 평균 좋아요 등)
    List<Object[]> getCategoryStatistics(PostStatus status, LocalDateTime fromDate);
    
    // 게시글 검색 with 하이라이트 (검색어 하이라이트 정보 포함)
    Page<Post> searchPostsWithHighlight(String keyword, 
                                       PostStatus status, 
                                       Pageable pageable);
    
    // 유사한 게시글 조회 (태그 기반 유사도)
    List<Post> findSimilarPosts(Long postId, int limit);
}