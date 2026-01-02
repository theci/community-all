package com.community.platform.moderation.dto;

import com.community.platform.moderation.domain.PenaltyType;
import com.community.platform.moderation.domain.UserPenalty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 제재 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class PenaltyResponse {

    private Long id;
    private Long userId;
    private String penaltyType;
    private String penaltyTypeName;
    private String reason;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Boolean isPermanent;
    private Long remainingMillis;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static PenaltyResponse from(UserPenalty penalty) {
        return PenaltyResponse.builder()
                .id(penalty.getId())
                .userId(penalty.getUserId())
                .penaltyType(penalty.getPenaltyType().name())
                .penaltyTypeName(penalty.getPenaltyType().getDisplayName())
                .reason(penalty.getReason())
                .startDate(penalty.getStartDate())
                .endDate(penalty.getEndDate())
                .isActive(penalty.isActive())
                .isPermanent(penalty.isPermanent())
                .remainingMillis(penalty.getRemainingMillis())
                .createdBy(penalty.getCreatedBy())
                .createdAt(penalty.getCreatedAt())
                .build();
    }
}
