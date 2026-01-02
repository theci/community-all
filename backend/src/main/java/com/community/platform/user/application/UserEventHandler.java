package com.community.platform.user.application;

import com.community.platform.user.domain.*;
import com.community.platform.engagement.application.ScrapFolderService;
import com.community.platform.notification.application.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 사용자 도메인 이벤트 처리 서비스
 * 사용자 관련 도메인 이벤트에 대한 후속 처리 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventHandler {

    private final ScrapFolderService scrapFolderService;
    private final NotificationPreferenceService notificationPreferenceService;

    /**
     * 사용자 회원가입 완료 이벤트 처리
     * - 기본 스크랩 폴더 생성
     * - 알림 설정 초기화
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("사용자 회원가입 이벤트 처리 시작. userId: {}, email: {}",
                event.getUserId(), event.getEmail());

        try {
            // 기본 스크랩 폴더 생성
            scrapFolderService.createDefaultFolder(event.getUserId());

            // 알림 설정 초기화 (모든 알림 활성화)
            notificationPreferenceService.createDefaultPreference(event.getUserId());

            log.info("사용자 회원가입 후속 처리 완료. userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("사용자 회원가입 후속 처리 실패. userId: {}, error: {}",
                    event.getUserId(), e.getMessage(), e);
            // 비즈니스 로직에 영향을 주지 않도록 예외를 삼킴
        }
    }

    /**
     * 사용자 차단 이벤트 처리
     * - 관리자 알림 전송 (향후 구현)
     * - 로그 기록 강화
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserBlocked(UserBlockedEvent event) {
        log.warn("사용자 차단 이벤트. userId: {}, reason: {}", 
                event.getUserId(), event.getReason());
        
        // TODO: 관리자 알림 전송
        // TODO: 감사 로그 기록
    }

    /**
     * 사용자 활성화 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserActivated(UserActivatedEvent event) {
        log.info("사용자 활성화 이벤트. userId: {}", event.getUserId());
        
        // TODO: 활성화 알림 전송
    }

    /**
     * 사용자 비활성화 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserDeactivated(UserDeactivatedEvent event) {
        log.info("사용자 비활성화 이벤트. userId: {}", event.getUserId());
        
        // TODO: 비활성화 관련 후속 처리
    }
}