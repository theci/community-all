package com.community.platform.messaging.exception;

import com.community.platform.shared.domain.BusinessException;

public class MessageNotFoundException extends BusinessException {
    public MessageNotFoundException(Long messageId) {
        super("MESSAGE_NOT_FOUND", "메시지를 찾을 수 없습니다. ID: " + messageId);
    }
}
