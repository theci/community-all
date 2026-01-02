package com.community.platform.content.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostMarkedAsNoticeEvent implements DomainEvent {
    private final Long postId;
    private final LocalDateTime occurredOn;

    public PostMarkedAsNoticeEvent(Long postId) {
        this.postId = postId;
        this.occurredOn = LocalDateTime.now();
    }
}