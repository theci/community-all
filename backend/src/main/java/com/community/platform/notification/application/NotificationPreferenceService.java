package com.community.platform.notification.application;

import com.community.platform.notification.domain.NotificationPreference;
import com.community.platform.notification.infrastructure.persistence.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 설정 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    /**
     * 기본 알림 설정 생성 (회원가입 시)
     */
    @Transactional
    public NotificationPreference createDefaultPreference(Long userId) {
        log.info("기본 알림 설정 생성. userId: {}", userId);

        // 이미 설정이 존재하는지 확인
        if (preferenceRepository.existsByUserId(userId)) {
            log.warn("이미 알림 설정이 존재합니다. userId: {}", userId);
            return preferenceRepository.findByUserId(userId).orElseThrow();
        }

        NotificationPreference preference = NotificationPreference.createDefault(userId);
        return preferenceRepository.save(preference);
    }

    /**
     * 알림 설정 조회
     */
    public NotificationPreference getPreference(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.warn("알림 설정이 없어 기본 설정 생성. userId: {}", userId);
                    return createDefaultPreference(userId);
                });
    }

    /**
     * 알림 설정 업데이트
     */
    @Transactional
    public NotificationPreference updatePreference(
            Long userId,
            Boolean notifyComment,
            Boolean notifyReply,
            Boolean notifyLike,
            Boolean notifyScrap,
            Boolean notifyReportResult,
            Boolean notifyLevelUp,
            Boolean notifyPenalty
    ) {
        log.info("알림 설정 업데이트. userId: {}", userId);

        NotificationPreference preference = getPreference(userId);
        preference.updatePreferences(
                notifyComment,
                notifyReply,
                notifyLike,
                notifyScrap,
                notifyReportResult,
                notifyLevelUp,
                notifyPenalty
        );

        return preference;
    }
}
