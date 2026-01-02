package com.community.platform.shared.domain;

/**
 * 비즈니스 로직 실행 중 발생하는 예외의 기본 클래스
 */
public class BusinessException extends RuntimeException {
    
    private final String errorCode;
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}