package com.community.platform.content.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostUpdatedEvent implements DomainEvent {
    private final Long postId;
    private final Long authorId;
    private final LocalDateTime occurredOn;

    public PostUpdatedEvent(Long postId, Long authorId) {
        this.postId = postId;
        this.authorId = authorId;
        this.occurredOn = LocalDateTime.now();
    }
}