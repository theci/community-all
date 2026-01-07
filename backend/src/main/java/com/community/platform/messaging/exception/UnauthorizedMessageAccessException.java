package com.community.platform.messaging.exception;

import com.community.platform.shared.domain.BusinessException;

public class UnauthorizedMessageAccessException extends BusinessException {
    public UnauthorizedMessageAccessException() {
        super("UNAUTHORIZED_MESSAGE_ACCESS", "메시지에 접근할 권한이 없습니다.");
    }
}
