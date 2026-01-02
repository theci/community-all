package com.community.platform.shared.infrastructure;

import com.community.platform.shared.domain.AggregateRoot;
import com.community.platform.shared.domain.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 도메인 이벤트 발행을 담당하는 인프라 서비스
 * 애그리거트에서 발생한 도메인 이벤트를 Spring Event로 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 애그리거트의 모든 도메인 이벤트를 발행하고 정리
     */
    public void publishEvents(AggregateRoot aggregate) {
        var events = aggregate.getDomainEvents();
        
        if (events.isEmpty()) {
            return;
        }
        
        log.debug("도메인 이벤트 발행. 이벤트 수: {}", events.size());
        
        // 모든 이벤트 발행
        events.forEach(this::publishEvent);
        
        // 이벤트 정리
        aggregate.clearDomainEvents();
    }

    /**
     * 단일 도메인 이벤트 발행
     */
    public void publishEvent(DomainEvent event) {
        log.debug("도메인 이벤트 발행: {}", event.getClass().getSimpleName());
        eventPublisher.publishEvent(event);
    }
}