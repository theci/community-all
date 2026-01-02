package com.community.platform.shared.security;

import com.community.platform.shared.domain.BusinessException;

/**
 * 보안 관련 예외의 기본 클래스
 */
public class SecurityException extends BusinessException {
    
    public SecurityException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public SecurityException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}