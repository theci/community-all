package com.community.platform.moderation.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 제재 생성 이벤트
 * 제재가 생성되면 알림을 보내거나 로그를 기록하는 데 사용
 */
@Getter
@AllArgsConstructor
public class UserPenaltyCreatedEvent implements DomainEvent {

    private final Long penaltyId;
    private final Long userId;
    private final PenaltyType penaltyType;
    private final String reason;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final LocalDateTime occurredOn;

    public UserPenaltyCreatedEvent(Long penaltyId, Long userId, PenaltyType penaltyType,
                                   String reason, LocalDateTime startDate, LocalDateTime endDate) {
        this.penaltyId = penaltyId;
        this.userId = userId;
        this.penaltyType = penaltyType;
        this.reason = reason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
