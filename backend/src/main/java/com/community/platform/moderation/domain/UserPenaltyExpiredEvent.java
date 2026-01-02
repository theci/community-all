package com.community.platform.moderation.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 제재 만료 이벤트
 * 제재가 만료되거나 해제될 때 발생
 */
@Getter
@AllArgsConstructor
public class UserPenaltyExpiredEvent implements DomainEvent {

    private final Long penaltyId;
    private final Long userId;
    private final PenaltyType penaltyType;
    private final LocalDateTime occurredOn;

    public UserPenaltyExpiredEvent(Long penaltyId, Long userId, PenaltyType penaltyType) {
        this.penaltyId = penaltyId;
        this.userId = userId;
        this.penaltyType = penaltyType;
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
