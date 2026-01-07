package com.community.platform.notification.application;

import com.community.platform.notification.domain.*;
import com.community.platform.notification.infrastructure.persistence.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 알림 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceService preferenceService;

    /**
     * 알림 발송
     * 사용자의 알림 설정을 확인하고 활성화된 경우에만 알림 생성
     */
    @Transactional
    public void sendNotification(
            Long userId,
            NotificationType type,
            String title,
            String content,
            Long relatedId,
            RelatedEntityType relatedType
    ) {
        // 자기 자신에게는 알림 발송 안 함
        if (userId == null) {
            log.debug("userId가 null이므로 알림을 발송하지 않습니다");
            return;
        }

        // 사용자의 알림 설정 확인
        if (!shouldSendNotification(userId, type)) {
            log.debug("사용자 알림 설정으로 인해 알림을 발송하지 않습니다. userId: {}, type: {}", userId, type);
            return;
        }

        // 중복 알림 체크 (좋아요, 스크랩만 체크 - 댓글은 여러 개 받을 수 있음)
        if (type == NotificationType.LIKE_ON_POST || type == NotificationType.SCRAP_ON_POST) {
            if (notificationRepository.existsByUserIdAndTypeAndRelatedId(userId, type, relatedId)) {
                log.debug("이미 발송된 알림이므로 중복 발송하지 않습니다. userId: {}, type: {}, relatedId: {}", userId, type, relatedId);
                return;
            }
        }

        // 알림 생성
        Notification notification = Notification.create(
                userId,
                type,
                title,
                content,
                relatedId,
                relatedType
        );

        notificationRepository.save(notification);
        log.info("알림 발송 완료. userId: {}, type: {}, title: {}", userId, type, title);
    }

    /**
     * 사용자의 알림 목록 조회
     */
    public Page<Notification> getMyNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다"));

        // 본인의 알림인지 확인
        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 알림만 읽음 처리할 수 있습니다");
        }

        notification.markAsRead();
        log.info("알림 읽음 처리. notificationId: {}, userId: {}", notificationId, userId);
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
        log.info("모든 알림 읽음 처리. userId: {}", userId);
    }

    /**
     * 알림 삭제
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        notificationRepository.deleteByIdAndUserId(notificationId, userId);
        log.info("알림 삭제. notificationId: {}, userId: {}", notificationId, userId);
    }

    /**
     * 알림 발송 여부 확인
     * 사용자의 알림 설정을 기반으로 판단
     */
    private boolean shouldSendNotification(Long userId, NotificationType type) {
        try {
            NotificationPreference preference = preferenceService.getPreference(userId);
            return preference.isEnabled(type);
        } catch (Exception e) {
            log.error("알림 설정 조회 실패. 기본값으로 발송. userId: {}, type: {}", userId, type, e);
            return true;  // 설정 조회 실패 시 기본적으로 발송
        }
    }

    /**
     * 읽지 않은 알림 조회
     */
    public Page<Notification> getUnreadNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findUnreadByUserId(userId, pageable);
    }
}
