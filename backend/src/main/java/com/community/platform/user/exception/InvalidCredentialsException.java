package com.community.platform.user.exception;

import com.community.platform.shared.domain.BusinessException;

/**
 * 잘못된 인증 정보로 인해 발생하는 예외
 */
public class InvalidCredentialsException extends BusinessException {
    
    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다");
    }
}