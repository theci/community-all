package com.community.platform.moderation.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 제재 자동 처리 스케줄러
 * 만료된 제재를 주기적으로 자동 해제
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PenaltyScheduler {

    private final UserPenaltyService penaltyService;

    /**
     * 만료된 제재 자동 해제
     * 5분마다 실행
     */
    @Scheduled(fixedDelay = 300000) // 5분 = 300,000ms
    public void expireOutdatedPenalties() {
        log.debug("만료된 제재 자동 처리 스케줄러 실행");

        try {
            penaltyService.checkAndAutoExpire();
        } catch (Exception e) {
            log.error("제재 자동 처리 중 오류 발생", e);
        }
    }
}
