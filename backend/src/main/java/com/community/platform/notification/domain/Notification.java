package com.community.platform.notification.domain;

import com.community.platform.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 엔티티
 * 사용자에게 발송되는 각종 알림을 저장
 */
@Entity
@Table(name = "notifications",
       indexes = {
           @Index(name = "idx_notification_user_read_created", columnList = "user_id, is_read, created_at"),
           @Index(name = "idx_notification_created_at", columnList = "created_at")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;  // 알림 수신자

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String content;

    @Column(name = "related_id")
    private Long relatedId;  // 관련 엔티티 ID (게시글 ID, 댓글 ID 등)

    @Enumerated(EnumType.STRING)
    @Column(name = "related_type", length = 20)
    private RelatedEntityType relatedType;  // 관련 엔티티 타입

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    private Notification(Long userId, NotificationType type, String title, String content,
                        Long relatedId, RelatedEntityType relatedType) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.relatedId = relatedId;
        this.relatedType = relatedType;
        this.isRead = false;
    }

    /**
     * 알림 생성
     */
    public static Notification create(
            Long userId,
            NotificationType type,
            String title,
            String content,
            Long relatedId,
            RelatedEntityType relatedType
    ) {
        return new Notification(userId, type, title, content, relatedId, relatedType);
    }

    /**
     * 알림 읽음 처리
     */
    public void markAsRead() {
        if (this.isRead) {
            return; // 이미 읽은 알림
        }
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * 읽지 않은 알림인지 확인
     */
    public boolean isUnread() {
        return !this.isRead;
    }
}
