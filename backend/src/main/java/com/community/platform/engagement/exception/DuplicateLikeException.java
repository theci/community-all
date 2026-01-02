package com.community.platform.engagement.exception;

import com.community.platform.shared.domain.BusinessException;

/**
 * 중복 좋아요 시 발생하는 예외
 */
public class DuplicateLikeException extends BusinessException {
    
    public DuplicateLikeException() {
        super("DUPLICATE_LIKE", "이미 좋아요를 누르셨습니다");
    }
}