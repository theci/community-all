package com.community.platform.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 설정 업데이트 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceUpdateRequest {

    private Boolean notifyComment;
    private Boolean notifyReply;
    private Boolean notifyLike;
    private Boolean notifyScrap;
    private Boolean notifyReportResult;
    private Boolean notifyLevelUp;
    private Boolean notifyPenalty;
}
