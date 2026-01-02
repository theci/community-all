package com.community.platform.shared.domain;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime getOccurredOn();
}