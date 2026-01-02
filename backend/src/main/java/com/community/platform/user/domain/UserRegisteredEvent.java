package com.community.platform.user.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserRegisteredEvent implements DomainEvent {
    private final Long userId;
    private final String email;
    private final String nickname;
    private final LocalDateTime occurredOn;

    public UserRegisteredEvent(Long userId, String email, String nickname) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.occurredOn = LocalDateTime.now();
    }
}