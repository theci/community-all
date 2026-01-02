package com.community.platform.shared.application;

import com.community.platform.shared.domain.AggregateRoot;
import com.community.platform.shared.infrastructure.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 도메인 이벤트 처리를 위한 애플리케이션 서비스
 * 서비스 계층에서 도메인 이벤트 발행을 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DomainEventService {

    private final DomainEventPublisher eventPublisher;

    /**
     * 애그리거트의 도메인 이벤트들을 일괄 발행
     * @param aggregate 이벤트를 발행할 애그리거트
     */
    public void publishEvents(AggregateRoot aggregate) {
        if (aggregate == null) {
            return;
        }
        
        var events = aggregate.getDomainEvents();
        if (events.isEmpty()) {
            return;
        }
        
        log.debug("도메인 이벤트 발행 시작. aggregate: {}, 이벤트 수: {}", 
                aggregate.getClass().getSimpleName(), events.size());
        
        try {
            eventPublisher.publishEvents(aggregate);
            log.debug("도메인 이벤트 발행 완료. aggregate: {}", aggregate.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("도메인 이벤트 발행 실패. aggregate: {}, error: {}", 
                    aggregate.getClass().getSimpleName(), e.getMessage(), e);
            // 이벤트 발행 실패가 주 비즈니스 로직에 영향을 주지 않도록 예외를 삼킴
        }
    }

    /**
     * 여러 애그리거트의 도메인 이벤트들을 일괄 발행
     * @param aggregates 이벤트를 발행할 애그리거트 목록
     */
    public void publishEvents(AggregateRoot... aggregates) {
        if (aggregates == null || aggregates.length == 0) {
            return;
        }
        
        for (AggregateRoot aggregate : aggregates) {
            publishEvents(aggregate);
        }
    }
}