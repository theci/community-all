package com.community.platform.content.application;

import com.community.platform.content.domain.Tag;
import com.community.platform.content.infrastructure.persistence.TagRepository;
import com.community.platform.content.infrastructure.persistence.PostTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 태그 관리 애플리케이션 서비스
 * 태그 생성, 관리 및 인기도 기반 추천 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    /**
     * 새 태그 생성
     */
    @Transactional
    public Tag createTag(String name, String color) {
        log.info("태그 생성 시작. name: {}, color: {}", name, color);
        
        // 태그명 중복 체크
        if (tagRepository.existsByName(name)) {
            // 기존 태그 반환
            return tagRepository.findByName(name).orElseThrow();
        }
        
        // 새 태그 생성
        Tag tag = Tag.create(name, color);
        Tag savedTag = tagRepository.save(tag);
        
        log.info("태그 생성 완료. tagId: {}", savedTag.getId());
        return savedTag;
    }

    /**
     * 태그 색상 수정
     */
    @Transactional
    public void updateTagColor(Long tagId, String color) {
        log.info("태그 색상 수정 처리. tagId: {}, color: {}", tagId, color);
        
        Tag tag = getTagById(tagId);
        tag.updateColor(color);
        
        log.info("태그 색상 수정 완료. tagId: {}", tagId);
    }

    /**
     * 태그 조회
     */
    public Tag getTagById(Long tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new TagNotFoundException(tagId));
    }

    /**
     * 태그명으로 조회
     */
    public Tag getTagByName(String name) {
        return tagRepository.findByName(name)
                .orElseThrow(() -> new TagNotFoundException(name));
    }

    /**
     * 인기 태그 조회 (사용 횟수 기준)
     */
    public Page<Tag> getPopularTags(Pageable pageable) {
        return tagRepository.findPopularTags(pageable);
    }

    /**
     * 상위 N개 인기 태그 조회
     */
    public List<Tag> getTopUsedTags(int limit) {
        return tagRepository.findTopUsedTags(PageRequest.of(0, limit));
    }

    /**
     * 태그 자동완성 검색
     */
    public List<Tag> searchTagsForAutocomplete(String keyword) {
        return tagRepository.findByNameContainingOrderByUsageCountDesc(keyword);
    }

    /**
     * 사용되지 않는 태그 조회 (정리용)
     */
    public List<Tag> getUnusedTags() {
        return tagRepository.findUnusedTags();
    }

    /**
     * 활성 태그 조회 (최소 사용 횟수 이상)
     */
    public List<Tag> getActiveTags(Long minUsageCount) {
        return tagRepository.findByUsageCountGreaterThanEqual(minUsageCount);
    }

    /**
     * 최근 생성된 태그 조회
     */
    public Page<Tag> getRecentTags(Pageable pageable) {
        return tagRepository.findRecentlyCreatedTags(pageable);
    }

    /**
     * 게시글의 태그 목록 조회
     */
    public List<Tag> getTagsByPost(Long postId) {
        return tagRepository.findByPostId(postId);
    }

    /**
     * 태그별 통계 조회 (태그별 게시글 수)
     */
    public List<Object[]> getTagStatistics() {
        return tagRepository.findTagsWithPostCount();
    }

    /**
     * 전체 태그 수 조회
     */
    public Long getTotalTagCount() {
        return tagRepository.countAllTags();
    }

    /**
     * 활성 태그 수 조회 (사용 중인 태그)
     */
    public Long getActiveTagCount() {
        return tagRepository.countActiveTags();
    }

    /**
     * 사용자가 자주 사용하는 태그 조회 (추천용)
     */
    public List<Object[]> getFrequentTagsByUser(Long authorId) {
        return postTagRepository.findFrequentTagsByAuthorId(authorId);
    }

    /**
     * 연관 태그 조회 (함께 많이 사용되는 태그)
     */
    public List<Object[]> getRelatedTags(Long tagId) {
        return postTagRepository.findRelatedTags(tagId);
    }

    /**
     * 여러 태그명으로 태그 조회 또는 생성
     */
    @Transactional
    public List<Tag> findOrCreateTags(List<String> tagNames) {
        log.info("태그 목록 조회/생성 처리. tagNames: {}", tagNames);
        
        List<Tag> existingTags = tagRepository.findByNameIn(tagNames);
        List<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .toList();
        
        // 새로 생성해야 할 태그들
        List<String> newTagNames = tagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .toList();
        
        // 새 태그들 생성
        List<Tag> newTags = newTagNames.stream()
                .map(name -> Tag.create(name, null))
                .toList();
        
        if (!newTags.isEmpty()) {
            tagRepository.saveAll(newTags);
            existingTags.addAll(newTags);
        }
        
        log.info("태그 목록 조회/생성 완료. 총 {}개", existingTags.size());
        return existingTags;
    }

    /**
     * 사용되지 않는 태그 정리 (배치 작업용)
     */
    @Transactional
    public void cleanupUnusedTags() {
        log.info("사용되지 않는 태그 정리 시작");
        
        List<Tag> unusedTags = getUnusedTags();
        if (!unusedTags.isEmpty()) {
            tagRepository.deleteAll(unusedTags);
            log.info("사용되지 않는 태그 {}개 삭제 완료", unusedTags.size());
        } else {
            log.info("삭제할 사용되지 않는 태그가 없습니다");
        }
    }

    /**
     * 태그를 찾을 수 없을 때 발생하는 예외
     */
    public static class TagNotFoundException extends RuntimeException {
        public TagNotFoundException(Long tagId) {
            super("태그를 찾을 수 없습니다. ID: " + tagId);
        }
        
        public TagNotFoundException(String tagName) {
            super("태그를 찾을 수 없습니다. Name: " + tagName);
        }
    }
}