package com.community.platform.content.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 카테고리 응답 DTO
 */
@Getter
@Builder
public class CategoryResponse {

    private Long id; // 카테고리 ID
    
    private Long parentCategoryId; // 부모 카테고리 ID
    
    private String name; // 카테고리명
    
    private String description; // 카테고리 설명
    
    private Integer displayOrder; // 표시 순서
    
    private Boolean isActive; // 활성 상태
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt; // 생성 시간
    
    private List<CategoryResponse> subCategories; // 하위 카테고리 목록
    
    private Long postCount; // 게시글 수 (통계용)
}