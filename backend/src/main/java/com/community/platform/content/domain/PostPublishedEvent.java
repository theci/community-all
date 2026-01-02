package com.community.platform.content.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostPublishedEvent implements DomainEvent {
    private final Long postId;
    private final Long authorId;
    private final String title;
    private final LocalDateTime occurredOn;

    public PostPublishedEvent(Long postId, Long authorId, String title) {
        this.postId = postId;
        this.authorId = authorId;
        this.title = title;
        this.occurredOn = LocalDateTime.now();
    }
}