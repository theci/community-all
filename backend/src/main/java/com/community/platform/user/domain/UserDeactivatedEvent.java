package com.community.platform.user.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserDeactivatedEvent implements DomainEvent {
    private final Long userId;
    private final LocalDateTime occurredOn;

    public UserDeactivatedEvent(Long userId) {
        this.userId = userId;
        this.occurredOn = LocalDateTime.now();
    }
}