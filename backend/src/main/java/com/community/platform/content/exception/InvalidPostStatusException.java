package com.community.platform.content.exception;

import com.community.platform.shared.domain.BusinessException;

/**
 * 게시글 상태 관련 예외 (발행, 삭제, 비공개 등)
 */
public class InvalidPostStatusException extends BusinessException {
    
    public InvalidPostStatusException(String message) {
        super("INVALID_POST_STATUS", message);
    }
    
    public static InvalidPostStatusException deleted() {
        return new InvalidPostStatusException("삭제된 게시글입니다");
    }
    
    public static InvalidPostStatusException draft() {
        return new InvalidPostStatusException("임시저장된 게시글입니다");
    }
    
    public static InvalidPostStatusException private_() {
        return new InvalidPostStatusException("비공개 게시글입니다");
    }
}