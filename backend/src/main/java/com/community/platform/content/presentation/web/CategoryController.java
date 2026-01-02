package com.community.platform.content.presentation.web;

import com.community.platform.content.application.CategoryService;
import com.community.platform.content.application.ContentMapper;
import com.community.platform.content.domain.Category;
import com.community.platform.content.dto.CategoryResponse;
import com.community.platform.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카테고리 관리 REST API Controller
 * 계층형 카테고리 구조 조회 및 관리 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final ContentMapper contentMapper;

    /**
     * 최상위 카테고리 목록 조회 (메뉴용)
     * GET /api/v1/categories/root
     */
    @GetMapping("/root")
    public ApiResponse<List<CategoryResponse>> getRootCategories() {
        log.debug("최상위 카테고리 목록 조회");

        List<Category> categories = categoryService.getRootCategories();
        List<CategoryResponse> response = categories.stream()
                .map(this::buildCategoryResponse)
                .toList();

        return ApiResponse.success(response);
    }

    /**
     * 전체 카테고리 계층 구조 조회 (관리자용)
     * GET /api/v1/categories/hierarchy
     */
    @GetMapping("/hierarchy")
    public ApiResponse<List<CategoryResponse>> getCategoryHierarchy() {
        log.debug("전체 카테고리 계층 구조 조회");

        List<Category> categories = categoryService.getAllCategoriesWithHierarchy();
        List<CategoryResponse> response = buildCategoryHierarchy(categories);

        return ApiResponse.success(response);
    }

    /**
     * 특정 카테고리 상세 조회
     * GET /api/v1/categories/{categoryId}
     */
    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> getCategory(@PathVariable Long categoryId) {
        log.debug("카테고리 상세 조회: categoryId={}", categoryId);

        Category category = categoryService.getCategoryById(categoryId);
        CategoryResponse response = buildCategoryResponse(category);

        return ApiResponse.success(response);
    }

    /**
     * 하위 카테고리 목록 조회
     * GET /api/v1/categories/{categoryId}/subcategories
     */
    @GetMapping("/{categoryId}/subcategories")
    public ApiResponse<List<CategoryResponse>> getSubCategories(@PathVariable Long categoryId) {
        log.debug("하위 카테고리 조회: parentCategoryId={}", categoryId);

        List<Category> subCategories = categoryService.getSubCategories(categoryId);
        List<CategoryResponse> response = subCategories.stream()
                .map(this::buildCategoryResponse)
                .toList();

        return ApiResponse.success(response);
    }

    /**
     * 카테고리 트리 구조 조회 (서브 카테고리 포함)
     * GET /api/v1/categories/tree
     */
    @GetMapping("/tree")
    public ApiResponse<List<CategoryResponse>> getCategoryTree() {
        log.debug("카테고리 트리 구조 조회");

        List<Category> categories = categoryService.getCategoryTree();
        List<CategoryResponse> response = categories.stream()
                .map(category -> {
                    List<CategoryResponse> subCategories = category.getSubCategories().stream()
                            .map(this::buildCategoryResponse)
                            .toList();
                    
                    return contentMapper.toCategoryResponseWithSubCategories(
                        category, subCategories, null); // TODO: 게시글 수 조회
                })
                .toList();

        return ApiResponse.success(response);
    }

    /**
     * 카테고리별 통계 조회 (게시글 수 포함)
     * GET /api/v1/categories/statistics
     */
    @GetMapping("/statistics")
    public ApiResponse<List<Object[]>> getCategoryStatistics() {
        log.debug("카테고리별 통계 조회");

        List<Object[]> statistics = categoryService.getCategoryStatistics();
        return ApiResponse.success(statistics);
    }

    /**
     * 빈 카테고리 목록 조회 (관리자용)
     * GET /api/v1/categories/empty
     */
    @GetMapping("/empty")
    public ApiResponse<List<CategoryResponse>> getEmptyCategories() {
        log.debug("빈 카테고리 목록 조회");

        List<Category> emptyCategories = categoryService.getEmptyCategories();
        List<CategoryResponse> response = contentMapper.toCategoryResponseList(emptyCategories);

        return ApiResponse.success(response);
    }

    // ========== Admin APIs (관리자 전용) ==========

    /**
     * 새 카테고리 생성 (관리자 전용)
     * POST /api/v1/categories
     */
    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(
            @RequestParam(required = false) Long parentCategoryId,
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        log.info("카테고리 생성: parentId={}, name={}", parentCategoryId, name);

        Category category = categoryService.createCategory(parentCategoryId, name, description);
        CategoryResponse response = buildCategoryResponse(category);

        return ApiResponse.success(response, "카테고리가 생성되었습니다");
    }

    /**
     * 카테고리 정보 수정 (관리자 전용)
     * PUT /api/v1/categories/{categoryId}
     */
    @PutMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer displayOrder) {
        log.info("카테고리 수정: categoryId={}, name={}", categoryId, name);

        categoryService.updateCategory(categoryId, name, description, displayOrder);
        Category category = categoryService.getCategoryById(categoryId);
        CategoryResponse response = buildCategoryResponse(category);

        return ApiResponse.success(response, "카테고리가 수정되었습니다");
    }

    /**
     * 카테고리 비활성화 (관리자 전용)
     * POST /api/v1/categories/{categoryId}/deactivate
     */
    @PostMapping("/{categoryId}/deactivate")
    public ApiResponse<Void> deactivateCategory(@PathVariable Long categoryId) {
        log.info("카테고리 비활성화: categoryId={}", categoryId);

        categoryService.deactivateCategory(categoryId);
        return ApiResponse.success("카테고리가 비활성화되었습니다");
    }

    /**
     * 카테고리 활성화 (관리자 전용)
     * POST /api/v1/categories/{categoryId}/activate
     */
    @PostMapping("/{categoryId}/activate")
    public ApiResponse<Void> activateCategory(@PathVariable Long categoryId) {
        log.info("카테고리 활성화: categoryId={}", categoryId);

        categoryService.activateCategory(categoryId);
        return ApiResponse.success("카테고리가 활성화되었습니다");
    }

    // ========== Private Helper Methods ==========

    /**
     * CategoryResponse 구성 (게시글 수 포함)
     */
    private CategoryResponse buildCategoryResponse(Category category) {
        // TODO: 실제 게시글 수 조회 로직 구현
        Long postCount = 0L; // 임시값
        
        return contentMapper.toCategoryResponseWithSubCategories(category, null, postCount);
    }

    /**
     * 카테고리 계층 구조 구성
     */
    private List<CategoryResponse> buildCategoryHierarchy(List<Category> categories) {
        // 실제로는 더 복잡한 계층 구조 로직이 필요
        // 여기서는 단순화된 구현
        return categories.stream()
                .map(this::buildCategoryResponse)
                .toList();
    }
}