package com.community.platform.notification.dto;

import com.community.platform.notification.domain.Notification;
import com.community.platform.notification.domain.NotificationType;
import com.community.platform.notification.domain.RelatedEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String content;
    private Long relatedId;
    private RelatedEntityType relatedType;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    /**
     * Notification 엔티티로부터 DTO 생성
     */
    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .relatedId(notification.getRelatedId())
                .relatedType(notification.getRelatedType())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
