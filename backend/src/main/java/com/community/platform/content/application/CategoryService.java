package com.community.platform.content.application;

import com.community.platform.content.domain.Category;
import com.community.platform.content.exception.CategoryNotFoundException;
import com.community.platform.content.infrastructure.persistence.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 카테고리 관리 애플리케이션 서비스
 * 계층형 카테고리 구조 관리 및 관련 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 새 카테고리 생성
     */
    @Transactional
    public Category createCategory(Long parentCategoryId, String name, String description) {
        log.info("카테고리 생성 시작. parentId: {}, name: {}", parentCategoryId, name);
        
        // 부모 카테고리 조회 (선택사항)
        Category parentCategory = null;
        if (parentCategoryId != null) {
            parentCategory = getCategoryById(parentCategoryId);
        }
        
        // 카테고리명 중복 체크
        validateCategoryNameNotDuplicated(name);
        
        // 표시 순서 결정
        Integer displayOrder = getNextDisplayOrder(parentCategoryId);
        
        // 카테고리 생성
        Category category = Category.create(parentCategory, name, description, displayOrder);
        Category savedCategory = categoryRepository.save(category);
        
        log.info("카테고리 생성 완료. categoryId: {}", savedCategory.getId());
        return savedCategory;
    }

    /**
     * 카테고리 정보 수정
     */
    @Transactional
    public void updateCategory(Long categoryId, String name, String description, Integer displayOrder) {
        log.info("카테고리 수정 처리. categoryId: {}", categoryId);
        
        Category category = getCategoryById(categoryId);
        
        // 카테고리명 중복 체크 (다른 카테고리와)
        if (!category.getName().equals(name)) {
            validateCategoryNameNotDuplicated(name);
        }
        
        // 카테고리 정보 수정
        category.updateInfo(name, description, displayOrder);
        
        log.info("카테고리 수정 완료. categoryId: {}", categoryId);
    }

    /**
     * 카테고리 비활성화
     */
    @Transactional
    public void deactivateCategory(Long categoryId) {
        log.info("카테고리 비활성화 처리. categoryId: {}", categoryId);
        
        Category category = getCategoryById(categoryId);
        
        // 하위 카테고리가 있는지 확인
        if (hasActiveSubCategories(categoryId)) {
            throw new IllegalStateException("하위 카테고리가 있는 카테고리는 비활성화할 수 없습니다.");
        }
        
        // 카테고리 비활성화
        category.deactivate();
        
        log.info("카테고리 비활성화 완료. categoryId: {}", categoryId);
    }

    /**
     * 카테고리 활성화
     */
    @Transactional
    public void activateCategory(Long categoryId) {
        log.info("카테고리 활성화 처리. categoryId: {}", categoryId);
        
        Category category = getCategoryById(categoryId);
        category.activate();
        
        log.info("카테고리 활성화 완료. categoryId: {}", categoryId);
    }

    /**
     * 카테고리 조회
     */
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }

    /**
     * 최상위 카테고리 목록 조회 (메뉴용)
     */
    public List<Category> getRootCategories() {
        return categoryRepository.findRootCategoriesOrderByDisplayOrder();
    }

    /**
     * 하위 카테고리 목록 조회
     */
    public List<Category> getSubCategories(Long parentCategoryId) {
        return categoryRepository.findByParentCategoryIdAndIsActiveOrderByDisplayOrder(parentCategoryId);
    }

    /**
     * 전체 카테고리 계층 구조 조회 (관리자용)
     */
    public List<Category> getAllCategoriesWithHierarchy() {
        return categoryRepository.findAllActiveCategoriesWithHierarchy();
    }

    /**
     * 카테고리 트리 구조 조회 (서브 카테고리 포함)
     */
    public List<Category> getCategoryTree() {
        return categoryRepository.findCategoryTreeWithSubCategories();
    }

    /**
     * 카테고리별 게시글 수 통계
     */
    public List<Object[]> getCategoryStatistics() {
        return categoryRepository.findCategoriesWithPostCount();
    }

    /**
     * 빈 카테고리 목록 조회 (게시글이 없는 카테고리)
     */
    public List<Category> getEmptyCategories() {
        return categoryRepository.findEmptyCategories();
    }

    /**
     * 카테고리명 중복 체크
     */
    private void validateCategoryNameNotDuplicated(String name) {
        if (categoryRepository.existsByNameAndIsActive(name, true)) {
            throw new IllegalArgumentException("이미 사용중인 카테고리명입니다: " + name);
        }
    }

    /**
     * 활성 하위 카테고리 존재 여부 확인
     */
    private boolean hasActiveSubCategories(Long categoryId) {
        return categoryRepository.countActiveSubCategories(categoryId) > 0;
    }

    /**
     * 다음 표시 순서 값 계산
     */
    private Integer getNextDisplayOrder(Long parentCategoryId) {
        Integer maxOrder = categoryRepository.findMaxDisplayOrderByParentCategory(parentCategoryId);
        return maxOrder + 1;
    }
}