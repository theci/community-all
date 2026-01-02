package com.community.platform.user.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserBlockedEvent implements DomainEvent {
    private final Long userId;
    private final String reason;
    private final LocalDateTime occurredOn;

    public UserBlockedEvent(Long userId, String reason) {
        this.userId = userId;
        this.reason = reason;
        this.occurredOn = LocalDateTime.now();
    }
}