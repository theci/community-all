package com.community.platform.notification.dto;

import com.community.platform.notification.domain.NotificationPreference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 알림 설정 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class NotificationPreferenceResponse {

    private Long id;
    private Long userId;
    private Boolean notifyComment;
    private Boolean notifyReply;
    private Boolean notifyLike;
    private Boolean notifyScrap;
    private Boolean notifyReportResult;
    private Boolean notifyLevelUp;
    private Boolean notifyPenalty;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * NotificationPreference 엔티티로부터 DTO 생성
     */
    public static NotificationPreferenceResponse from(NotificationPreference preference) {
        return NotificationPreferenceResponse.builder()
                .id(preference.getId())
                .userId(preference.getUserId())
                .notifyComment(preference.getNotifyComment())
                .notifyReply(preference.getNotifyReply())
                .notifyLike(preference.getNotifyLike())
                .notifyScrap(preference.getNotifyScrap())
                .notifyReportResult(preference.getNotifyReportResult())
                .notifyLevelUp(preference.getNotifyLevelUp())
                .notifyPenalty(preference.getNotifyPenalty())
                .createdAt(preference.getCreatedAt())
                .updatedAt(preference.getUpdatedAt())
                .build();
    }
}
