package com.community.platform.engagement.application;

import com.community.platform.engagement.domain.PostScrap;
import com.community.platform.engagement.domain.ScrapFolder;
import com.community.platform.engagement.infrastructure.persistence.PostScrapRepository;
import com.community.platform.engagement.infrastructure.persistence.ScrapFolderRepository;
import com.community.platform.content.infrastructure.persistence.PostRepository;
import com.community.platform.user.infrastructure.persistence.UserRepository;
import com.community.platform.content.exception.PostNotFoundException;
import com.community.platform.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 스크랩 관리 애플리케이션 서비스
 * 스크랩 추가/제거, 폴더 관리 및 관련 통계 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostScrapService {

    private final PostScrapRepository postScrapRepository;
    private final ScrapFolderRepository scrapFolderRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 게시글 스크랩 추가 (기본 폴더에)
     */
    @Transactional
    public void addPostScrap(Long userId, Long postId) {
        log.info("게시글 스크랩 추가. userId: {}, postId: {}", userId, postId);
        
        // 기본 폴더 조회 또는 생성
        ScrapFolder defaultFolder = getOrCreateDefaultFolder(userId);
        
        addPostScrapToFolder(userId, postId, defaultFolder.getId());
    }

    /**
     * 게시글 스크랩 추가 (특정 폴더에)
     */
    @Transactional
    public void addPostScrapToFolder(Long userId, Long postId, Long folderId) {
        log.info("게시글 스크랩 추가. userId: {}, postId: {}, folderId: {}", userId, postId, folderId);
        
        // 사용자 및 게시글 존재 확인
        validateUserExists(userId);
        validatePostExists(postId);
        
        // 스크랩 폴더 조회
        ScrapFolder folder = getScrapFolderById(folderId);
        
        // 폴더 소유자 확인
        validateFolderOwnership(folder, userId);
        
        // 중복 스크랩 확인
        if (postScrapRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new IllegalStateException("이미 스크랩한 게시글입니다.");
        }
        
        // 스크랩 생성
        PostScrap postScrap = PostScrap.create(postId, userId, folder);
        postScrapRepository.save(postScrap);
        
        log.info("게시글 스크랩 추가 완료. userId: {}, postId: {}", userId, postId);
    }

    /**
     * 게시글 스크랩 제거
     */
    @Transactional
    public void removePostScrap(Long userId, Long postId) {
        log.info("게시글 스크랩 제거. userId: {}, postId: {}", userId, postId);
        
        // 스크랩 존재 확인
        if (!postScrapRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new IllegalStateException("스크랩하지 않은 게시글입니다.");
        }
        
        // 스크랩 삭제
        postScrapRepository.deleteByUserIdAndPostId(userId, postId);
        
        log.info("게시글 스크랩 제거 완료. userId: {}, postId: {}", userId, postId);
    }

    /**
     * 스크랩 폴더 이동
     */
    @Transactional
    public void moveScrapToFolder(Long userId, Long postId, Long newFolderId) {
        log.info("스크랩 폴더 이동. userId: {}, postId: {}, newFolderId: {}", userId, postId, newFolderId);
        
        // 기존 스크랩 조회
        PostScrap postScrap = postScrapRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new IllegalStateException("스크랩하지 않은 게시글입니다."));
        
        // 새 폴더 조회
        ScrapFolder newFolder = getScrapFolderById(newFolderId);
        
        // 폴더 소유자 확인
        validateFolderOwnership(newFolder, userId);
        
        // 폴더 변경
        postScrap.updateScrapFolder(newFolder);
        
        log.info("스크랩 폴더 이동 완료. userId: {}, postId: {}", userId, postId);
    }

    /**
     * 사용자의 게시글 스크랩 상태 확인
     */
    public boolean isPostScrappedByUser(Long userId, Long postId) {
        return postScrapRepository.existsByUserIdAndPostId(userId, postId);
    }

    /**
     * 사용자의 모든 스크랩 조회
     */
    public Page<PostScrap> getUserScraps(Long userId, Pageable pageable) {
        return postScrapRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 특정 폴더의 스크랩 목록 조회
     */
    public Page<PostScrap> getScrapsByFolder(Long folderId, Pageable pageable) {
        return postScrapRepository.findByScrapFolderIdOrderByCreatedAtDesc(folderId, pageable);
    }

    /**
     * 사용자의 특정 폴더 스크랩 조회
     */
    public Page<PostScrap> getUserScrapsByFolder(Long userId, Long folderId, Pageable pageable) {
        return postScrapRepository.findByUserIdAndScrapFolderIdOrderByCreatedAtDesc(
                userId, folderId, pageable);
    }

    /**
     * 게시글의 스크랩 개수 조회
     */
    public Long getPostScrapCount(Long postId) {
        return postScrapRepository.countByPostId(postId);
    }

    /**
     * 사용자별 스크랩 개수 조회
     */
    public Long getUserScrapCount(Long userId) {
        return postScrapRepository.countByUserId(userId);
    }

    /**
     * 최근 스크랩 활동 조회
     */
    public Page<PostScrap> getRecentScrapActivities(Pageable pageable) {
        return postScrapRepository.findRecentScraps(pageable);
    }

    /**
     * 특정 기간 동안 게시글 스크랩 개수 조회
     */
    public Long getPostScrapCountInPeriod(Long postId, LocalDateTime startDate, LocalDateTime endDate) {
        return postScrapRepository.countByPostIdAndCreatedAtBetween(postId, startDate, endDate);
    }

    /**
     * 가장 많이 스크랩된 게시글 조회
     */
    public List<Object[]> getMostScrappedPosts(int daysAgo, Pageable pageable) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(daysAgo);
        return postScrapRepository.findMostScrappedPostIds(fromDate, pageable);
    }

    /**
     * 여러 게시글에 대한 사용자 스크랩 상태 조회
     */
    public List<PostScrap> getUserScrapsForPosts(Long userId, List<Long> postIds) {
        return postScrapRepository.findByUserIdAndPostIdIn(userId, postIds);
    }

    /**
     * 기본 스크랩 폴더 조회 또는 생성
     */
    private ScrapFolder getOrCreateDefaultFolder(Long userId) {
        return scrapFolderRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseGet(() -> {
                    ScrapFolder defaultFolder = ScrapFolder.createDefault(userId);
                    return scrapFolderRepository.save(defaultFolder);
                });
    }

    /**
     * 스크랩 폴더 조회
     */
    private ScrapFolder getScrapFolderById(Long folderId) {
        return scrapFolderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("스크랩 폴더를 찾을 수 없습니다. ID: " + folderId));
    }

    /**
     * 폴더 소유자 확인
     */
    private void validateFolderOwnership(ScrapFolder folder, Long userId) {
        if (!folder.getUserId().equals(userId)) {
            throw new IllegalArgumentException("다른 사용자의 스크랩 폴더에는 접근할 수 없습니다.");
        }
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

    // ========== Controller 메서드 래퍼 ==========

    /**
     * 게시글 스크랩 (Controller용)
     */
    @Transactional
    public PostScrap scrapPost(Long userId, Long postId, Long folderId) {
        log.info("게시글 스크랩. userId: {}, postId: {}, folderId: {}", userId, postId, folderId);

        if (folderId == null) {
            // 기본 폴더에 스크랩
            addPostScrap(userId, postId);
        } else {
            // 특정 폴더에 스크랩
            addPostScrapToFolder(userId, postId, folderId);
        }

        // 생성된 스크랩 반환
        return postScrapRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new IllegalStateException("스크랩 생성에 실패했습니다."));
    }

    /**
     * 게시글 스크랩 취소 (Controller용)
     */
    @Transactional
    public void unscrapPost(Long userId, Long postId) {
        removePostScrap(userId, postId);
    }

    /**
     * 사용자의 스크랩 여부 확인 (Controller용)
     */
    public boolean isScrapedByUser(Long userId, Long postId) {
        return isPostScrappedByUser(userId, postId);
    }

    /**
     * 게시글 스크랩 수 조회 (Controller용)
     */
    public Long getScrapCount(Long postId) {
        return getPostScrapCount(postId);
    }

    /**
     * 특정 폴더의 스크랩 조회 (Controller용)
     */
    public Page<PostScrap> getScrapsByFolder(Long userId, Long folderId, Pageable pageable) {
        return getUserScrapsByFolder(userId, folderId, pageable);
    }

    /**
     * 사용자 스크랩 검색
     */
    public Page<PostScrap> searchUserScraps(Long userId, String keyword, Long folderId, Pageable pageable) {
        // 간단한 구현 - 실제로는 Repository에 검색 메서드 필요
        if (folderId != null) {
            return getUserScrapsByFolder(userId, folderId, pageable);
        }
        return getUserScraps(userId, pageable);
    }

    /**
     * 중복 스크랩 조회
     */
    public Page<Object[]> getDuplicateScraps(Long userId, Pageable pageable) {
        // 간단한 구현 - 실제로는 더 복잡한 쿼리 필요
        List<Object[]> emptyList = List.of();
        return org.springframework.data.support.PageableExecutionUtils.getPage(
            emptyList, pageable, () -> 0L
        );
    }

    /**
     * 최근 스크랩 조회
     */
    public Page<PostScrap> getRecentScraps(Long userId, int days, Pageable pageable) {
        // 간단한 구현
        return getUserScraps(userId, pageable);
    }

    /**
     * 사용자 스크랩 통계
     */
    public Object[] getUserScrapStatistics(Long userId, int days) {
        Long totalScraps = getUserScrapCount(userId);
        return new Object[]{totalScraps, days};
    }

    /**
     * 인기 스크랩 게시글 조회
     */
    public Page<Object[]> getPopularScrappedPosts(int days, Pageable pageable) {
        List<Object[]> results = getMostScrappedPosts(days, pageable);
        return org.springframework.data.support.PageableExecutionUtils.getPage(
            results, pageable, results::size
        );
    }

    /**
     * 전체 스크랩 통계
     */
    public Object[] getScrapStatistics(int days) {
        Long totalScraps = postScrapRepository.count();
        return new Object[]{totalScraps, days};
    }
}