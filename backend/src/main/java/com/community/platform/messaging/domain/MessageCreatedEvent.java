package com.community.platform.messaging.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageCreatedEvent implements DomainEvent {
    private final Long messageId;
    private final Long threadId;
    private final Long senderId;
    private final Long recipientId;
    private final String content;
    private final LocalDateTime occurredOn;

    public MessageCreatedEvent(Long messageId, Long threadId, Long senderId, Long recipientId, String content) {
        this.messageId = messageId;
        this.threadId = threadId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.occurredOn = LocalDateTime.now();
    }
}
