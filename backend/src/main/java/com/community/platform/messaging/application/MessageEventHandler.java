package com.community.platform.messaging.application;

import com.community.platform.messaging.domain.MessageCreatedEvent;
import com.community.platform.notification.application.NotificationService;
import com.community.platform.notification.domain.NotificationType;
import com.community.platform.notification.domain.RelatedEntityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 메시지 이벤트 핸들러
 * 메시지 도메인 이벤트를 수신하여 알림 발송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageEventHandler {

    private final NotificationService notificationService;

    /**
     * 메시지 생성 이벤트 처리
     * 수신자에게 알림 발송
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageCreated(MessageCreatedEvent event) {
        log.debug("메시지 생성 이벤트 수신. messageId: {}, threadId: {}, senderId: {}, recipientId: {}",
                event.getMessageId(), event.getThreadId(), event.getSenderId(), event.getRecipientId());

        // 수신자에게 알림 발송 (threadId를 relatedId로 사용하여 해당 대화로 바로 이동 가능)
        notificationService.sendNotification(
                event.getRecipientId(),
                NotificationType.MESSAGE_RECEIVED,
                "새 쪽지",
                "새로운 쪽지가 도착했습니다",
                event.getThreadId(),  // threadId를 사용하여 특정 대화로 이동
                RelatedEntityType.MESSAGE
        );

        log.info("메시지 알림 발송 완료. messageId: {}, threadId: {}, recipientId: {}",
                event.getMessageId(), event.getThreadId(), event.getRecipientId());
    }
}
