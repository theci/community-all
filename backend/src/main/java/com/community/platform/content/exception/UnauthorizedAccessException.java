package com.community.platform.content.exception;

import com.community.platform.shared.domain.BusinessException;

/**
 * 권한 없는 접근 시 발생하는 예외 (작성자가 아닌 경우 등)
 */
public class UnauthorizedAccessException extends BusinessException {
    
    public UnauthorizedAccessException() {
        super("UNAUTHORIZED_ACCESS", "접근 권한이 없습니다");
    }
    
    public UnauthorizedAccessException(String message) {
        super("UNAUTHORIZED_ACCESS", message);
    }
    
    public static UnauthorizedAccessException notAuthor() {
        return new UnauthorizedAccessException("작성자만 수정/삭제할 수 있습니다");
    }
}