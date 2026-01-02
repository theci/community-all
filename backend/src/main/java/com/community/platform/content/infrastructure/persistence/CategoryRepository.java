package com.community.platform.content.infrastructure.persistence;

import com.community.platform.content.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 활성 상태인 최상위 카테고리 목록 조회 (메뉴 표시용)
    @Query("SELECT c FROM Category c WHERE c.parentCategory IS NULL AND c.isActive = true ORDER BY c.displayOrder ASC")
    List<Category> findRootCategoriesOrderByDisplayOrder();
    
    // 특정 상위 카테고리의 하위 카테고리 목록 조회 (서브메뉴 표시용)
    @Query("SELECT c FROM Category c WHERE c.parentCategory.id = :parentCategoryId AND c.isActive = true ORDER BY c.displayOrder ASC")
    List<Category> findByParentCategoryIdAndIsActiveOrderByDisplayOrder(@Param("parentCategoryId") Long parentCategoryId);
    
    // 모든 활성 카테고리를 계층 구조로 조회 (관리자용)
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY " +
           "CASE WHEN c.parentCategory IS NULL THEN c.displayOrder ELSE c.parentCategory.displayOrder END, " +
           "c.parentCategory.id ASC NULLS FIRST, c.displayOrder ASC")
    List<Category> findAllActiveCategoriesWithHierarchy();
    
    // 카테고리명으로 검색 (카테고리 중복 체크용)
    Optional<Category> findByNameAndIsActive(String name, Boolean isActive);
    
    // 카테고리명 존재 여부 확인 (카테고리 생성 시 중복 체크)
    boolean existsByNameAndIsActive(String name, Boolean isActive);
    
    // 특정 상위 카테고리의 하위 카테고리 개수 조회 (하위 카테고리 존재 체크)
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parentCategory.id = :parentCategoryId AND c.isActive = true")
    Long countActiveSubCategories(@Param("parentCategoryId") Long parentCategoryId);
    
    // 카테고리 트리 전체 조회 (계층형 메뉴 구성용)
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subCategories sc WHERE c.parentCategory IS NULL AND c.isActive = true ORDER BY c.displayOrder ASC")
    List<Category> findCategoryTreeWithSubCategories();
    
    // 특정 깊이의 카테고리만 조회 (1레벨, 2레벨 카테고리 구분용)
    @Query("SELECT c FROM Category c WHERE " +
           "CASE WHEN :depth = 0 THEN c.parentCategory IS NULL " +
           "     WHEN :depth = 1 THEN c.parentCategory IS NOT NULL AND c.parentCategory.parentCategory IS NULL " +
           "     ELSE false END " +
           "AND c.isActive = true ORDER BY c.displayOrder ASC")
    List<Category> findByDepthAndIsActive(@Param("depth") int depth);
    
    // 카테고리별 게시글 수와 함께 조회 (통계용)
    @Query("SELECT c, COUNT(p) as postCount FROM Category c " +
           "LEFT JOIN Post p ON p.category.id = c.id AND p.status = 'PUBLISHED' " +
           "WHERE c.isActive = true " +
           "GROUP BY c " +
           "ORDER BY c.displayOrder ASC")
    List<Object[]> findCategoriesWithPostCount();
    
    // 사용되지 않는 빈 카테고리 조회 (관리자의 카테고리 정리용)
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND " +
           "NOT EXISTS (SELECT 1 FROM Post p WHERE p.category.id = c.id AND p.status = 'PUBLISHED')")
    List<Category> findEmptyCategories();
    
    // 최대 표시 순서 값 조회 (새 카테고리의 표시 순서 결정용)
    @Query("SELECT COALESCE(MAX(c.displayOrder), 0) FROM Category c WHERE " +
           "(:parentCategoryId IS NULL AND c.parentCategory IS NULL) OR " +
           "(:parentCategoryId IS NOT NULL AND c.parentCategory.id = :parentCategoryId)")
    Integer findMaxDisplayOrderByParentCategory(@Param("parentCategoryId") Long parentCategoryId);
}