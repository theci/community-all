package com.community.platform.user.exception;

import com.community.platform.shared.domain.BusinessException;

/**
 * 중복된 사용자 정보로 인해 발생하는 예외
 */
public class DuplicateUserException extends BusinessException {
    
    public DuplicateUserException(String field, String value) {
        super("DUPLICATE_USER", String.format("이미 사용중인 %s입니다: %s", field, value));
    }
}