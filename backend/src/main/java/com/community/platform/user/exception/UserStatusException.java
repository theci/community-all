package com.community.platform.user.exception;

import com.community.platform.shared.domain.BusinessException;

/**
 * 사용자 상태 관련 예외 (차단, 비활성화 등)
 */
public class UserStatusException extends BusinessException {
    
    public UserStatusException(String message) {
        super("USER_STATUS_ERROR", message);
    }
    
    public static UserStatusException blocked() {
        return new UserStatusException("차단된 사용자입니다");
    }
    
    public static UserStatusException inactive() {
        return new UserStatusException("비활성화된 사용자입니다");
    }
    
    public static UserStatusException deleted() {
        return new UserStatusException("삭제된 사용자입니다");
    }
}