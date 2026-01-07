package com.community.platform.messaging.exception;

import com.community.platform.shared.domain.BusinessException;

public class MessageThreadNotFoundException extends BusinessException {
    public MessageThreadNotFoundException(Long threadId) {
        super("MESSAGE_THREAD_NOT_FOUND", "대화 스레드를 찾을 수 없습니다. ID: " + threadId);
    }
}
