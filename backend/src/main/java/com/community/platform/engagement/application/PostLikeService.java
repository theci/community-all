package com.community.platform.engagement.application;

import com.community.platform.engagement.domain.PostLike;
import com.community.platform.engagement.infrastructure.persistence.PostLikeRepository;
import com.community.platform.content.infrastructure.persistence.PostRepository;
import com.community.platform.user.infrastructure.persistence.UserRepository;
import com.community.platform.content.exception.PostNotFoundException;
import com.community.platform.user.exception.UserNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 좋아요 관리 애플리케이션 서비스
 * 좋아요/좋아요 취소 및 관련 통계 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 게시글 좋아요 추가
     */
    @Transactional
    public void addPostLike(Long userId, Long postId) {
        log.info("게시글 좋아요 추가. userId: {}, postId: {}", userId, postId);
        
        // 사용자 및 게시글 존재 확인
        validateUserExists(userId);
        validatePostExists(postId);
        
        // 중복 좋아요 확인
        if (postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new IllegalStateException("이미 좋아요를 누른 게시글입니다.");
        }
        
        // 좋아요 생성
        PostLike postLike = PostLike.create(postId, userId);
        postLikeRepository.save(postLike);
        
        // 게시글 좋아요 수 증가
        postRepository.incrementLikeCount(postId);
        
        log.info("게시글 좋아요 추가 완료. userId: {}, postId: {}", userId, postId);
    }

    /**
     * 게시글 좋아요 취소
     */
    @Transactional
    public void removePostLike(Long userId, Long postId) {
        log.info("게시글 좋아요 취소. userId: {}, postId: {}", userId, postId);
        
        // 좋아요 존재 확인
        if (!postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new IllegalStateException("좋아요를 누르지 않은 게시글입니다.");
        }
        
        // 좋아요 삭제
        postLikeRepository.deleteByUserIdAndPostId(userId, postId);
        
        // 게시글 좋아요 수 감소
        postRepository.decrementLikeCount(postId);
        
        log.info("게시글 좋아요 취소 완료. userId: {}, postId: {}", userId, postId);
    }

    /**
     * 게시글 좋아요 상태 토글 (좋아요 ↔ 좋아요 취소)
     * Controller용 메서드 - LikeResult 반환
     */
    @Transactional
    public LikeResult toggleLike(Long userId, Long postId) {
        log.info("게시글 좋아요 상태 토글. userId: {}, postId: {}", userId, postId);

        boolean isLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);

        if (isLiked) {
            removePostLike(userId, postId);
        } else {
            addPostLike(userId, postId);
        }

        Long totalCount = postLikeRepository.countByPostId(postId);
        return new LikeResult(!isLiked, totalCount);
    }

    /**
     * 사용자의 게시글 좋아요 상태 확인
     */
    public boolean isLikedByUser(Long userId, Long postId) {
        return postLikeRepository.existsByUserIdAndPostId(userId, postId);
    }

    /**
     * 게시글의 좋아요 개수 조회
     */
    public Long getLikeCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    /**
     * 사용자가 좋아요한 게시글 목록 조회
     */
    public Page<PostLike> getUserLikes(Long userId, Pageable pageable) {
        return postLikeRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 게시글을 좋아요한 사용자 목록 조회
     */
    public Page<PostLike> getPostLikes(Long postId, Pageable pageable) {
        return postLikeRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);
    }

    /**
     * 사용자별 좋아요 개수 조회
     */
    public Long getUserLikeCount(Long userId) {
        return postLikeRepository.countByUserId(userId);
    }

    /**
     * 최근 좋아요 활동 조회 (피드용)
     */
    public Page<PostLike> getRecentLikeActivities(Pageable pageable) {
        return postLikeRepository.findRecentLikes(pageable);
    }

    /**
     * 특정 기간 동안 게시글 좋아요 개수 조회
     */
    public Long getPostLikeCountInPeriod(Long postId, LocalDateTime startDate, LocalDateTime endDate) {
        return postLikeRepository.countByPostIdAndCreatedAtBetween(postId, startDate, endDate);
    }

    /**
     * 가장 인기있는 게시글 ID 조회 (좋아요 기준)
     */
    public List<Object[]> getMostLikedPosts(int daysAgo, Pageable pageable) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(daysAgo);
        return postLikeRepository.findMostLikedPostIds(fromDate, pageable);
    }

    /**
     * 좋아요 활동 시간대 분석
     */
    public List<Object[]> getLikeActivityByHour(LocalDateTime startDate, LocalDateTime endDate) {
        return postLikeRepository.findLikeActivityByHour(startDate, endDate);
    }

    /**
     * 여러 게시글에 대한 사용자 좋아요 상태 조회 (배치 처리용)
     */
    public List<PostLike> getUserLikesForPosts(Long userId, List<Long> postIds) {
        return postLikeRepository.findByUserIdAndPostIdIn(userId, postIds);
    }

    /**
     * 사용자 존재 여부 확인
     */
    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }

    /**
     * 게시글 존재 여부 확인
     */
    private void validatePostExists(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(postId);
        }
    }

    /**
     * 인기 게시글 조회 (좋아요 수 기준)
     */
    public Page<Object[]> getPopularPosts(int days, Pageable pageable) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        List<Object[]> results = postLikeRepository.findMostLikedPostIds(fromDate, pageable);
        // 간단한 Page 래퍼 반환
        return org.springframework.data.support.PageableExecutionUtils.getPage(
            results, pageable, results::size
        );
    }

    /**
     * 트렌딩 게시글 조회 (최근 급상승)
     */
    public Page<Object[]> getTrendingPosts(int hours, Pageable pageable) {
        LocalDateTime fromDate = LocalDateTime.now().minusHours(hours);
        List<Object[]> results = postLikeRepository.findMostLikedPostIds(fromDate, pageable);
        return org.springframework.data.support.PageableExecutionUtils.getPage(
            results, pageable, results::size
        );
    }

    /**
     * 좋아요 통계 조회
     */
    public Object[] getLikeStatistics(int days) {
        // 간단한 통계 구현 - 실제로는 더 복잡한 통계 로직 필요
        Long totalLikes = postLikeRepository.count();
        return new Object[]{totalLikes, days};
    }

    /**
     * 사용자별 좋아요 활동 조회
     */
    public Object[] getUserLikeActivity(Long userId, int days) {
        Long userLikeCount = postLikeRepository.countByUserId(userId);
        return new Object[]{userId, userLikeCount, days};
    }

    /**
     * 의심스러운 좋아요 패턴 감지
     */
    public Page<Object[]> getSuspiciousLikePatterns(Pageable pageable) {
        // 간단한 구현 - 실제로는 더 복잡한 로직 필요
        List<Object[]> emptyList = List.of();
        return org.springframework.data.support.PageableExecutionUtils.getPage(
            emptyList, pageable, () -> 0L
        );
    }

    /**
     * 좋아요 토글 결과를 담는 DTO
     */
    @Getter
    @AllArgsConstructor
    public static class LikeResult {
        private final boolean isLiked;
        private final Long totalLikeCount;
    }
}