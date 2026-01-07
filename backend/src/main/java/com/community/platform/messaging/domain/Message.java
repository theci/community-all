package com.community.platform.messaging.domain;

import com.community.platform.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages",
       indexes = {
           @Index(name = "idx_message_recipient_status_created", columnList = "recipient_id, status, created_at"),
           @Index(name = "idx_message_sender_created", columnList = "sender_id, created_at"),
           @Index(name = "idx_message_thread_created", columnList = "thread_id, created_at"),
           @Index(name = "idx_message_recipient_read", columnList = "recipient_id, is_read, created_at")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends AggregateRoot {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id")
    private MessageThread thread;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageStatus status = MessageStatus.ACTIVE;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    private Message(MessageThread thread, Long senderId, Long recipientId, String content) {
        this.thread = thread;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.status = MessageStatus.ACTIVE;
        this.isRead = false;
    }

    public static Message create(MessageThread thread, Long senderId, Long recipientId, String content) {
        validateContent(content);
        validateUsers(senderId, recipientId);

        Message message = new Message(thread, senderId, recipientId, content);

        // 메시지 생성 이벤트 발행 (알림용)
        message.addDomainEvent(new MessageCreatedEvent(
                message.getId(),
                senderId,
                recipientId,
                content
        ));

        // 스레드의 마지막 메시지 시간 업데이트
        if (thread != null) {
            thread.updateLastMessageAt();
        }

        return message;
    }

    private static void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용은 비어있을 수 없습니다.");
        }
        if (content.length() > 5000) {
            throw new IllegalArgumentException("메시지 내용은 5000자를 초과할 수 없습니다.");
        }
    }

    private static void validateUsers(Long senderId, Long recipientId) {
        if (senderId == null || recipientId == null) {
            throw new IllegalArgumentException("발신자와 수신자 ID는 필수입니다.");
        }
        if (senderId.equals(recipientId)) {
            throw new IllegalArgumentException("자기 자신에게 메시지를 보낼 수 없습니다.");
        }
    }

    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }

    public void delete() {
        this.status = MessageStatus.DELETED;
    }

    public boolean canBeReadBy(Long userId) {
        return this.recipientId.equals(userId);
    }

    public boolean canBeDeletedBy(Long userId) {
        return this.senderId.equals(userId) || this.recipientId.equals(userId);
    }
}
