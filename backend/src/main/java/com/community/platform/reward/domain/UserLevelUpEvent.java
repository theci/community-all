package com.community.platform.reward.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 레벨업 도메인 이벤트
 * 알림 시스템에서 사용
 */
@Getter
@RequiredArgsConstructor
public class UserLevelUpEvent implements DomainEvent {

    private final Long userId;
    private final UserLevel oldLevel;
    private final UserLevel newLevel;
    private final Long totalPoints;
    private final LocalDateTime occurredOn = LocalDateTime.now();
}
