package com.community.platform.user.exception;

import com.community.platform.shared.domain.BusinessException;

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 */
public class UserNotFoundException extends BusinessException {
    
    public UserNotFoundException(Long userId) {
        super("USER_NOT_FOUND", "사용자를 찾을 수 없습니다. ID: " + userId);
    }
    
    public UserNotFoundException(String email) {
        super("USER_NOT_FOUND", "사용자를 찾을 수 없습니다. Email: " + email);
    }
}