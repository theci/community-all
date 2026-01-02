package com.community.platform.content.exception;

import com.community.platform.shared.domain.BusinessException;

/**
 * 카테고리를 찾을 수 없을 때 발생하는 예외
 */
public class CategoryNotFoundException extends BusinessException {
    
    public CategoryNotFoundException(Long categoryId) {
        super("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다. ID: " + categoryId);
    }
}