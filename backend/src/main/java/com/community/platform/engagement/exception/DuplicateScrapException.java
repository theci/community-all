package com.community.platform.engagement.exception;

import com.community.platform.shared.domain.BusinessException;

/**
 * 중복 스크랩 시 발생하는 예외
 */
public class DuplicateScrapException extends BusinessException {
    
    public DuplicateScrapException() {
        super("DUPLICATE_SCRAP", "이미 스크랩된 게시글입니다");
    }
}