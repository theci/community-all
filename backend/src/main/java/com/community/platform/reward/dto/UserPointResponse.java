package com.community.platform.reward.dto;

import com.community.platform.reward.domain.UserLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 포인트 응답 DTO
 */
@Getter
@Builder
public class UserPointResponse {
    private Long id;
    private Long userId;
    private Integer totalPoints;
    private Integer availablePoints;
    private UserLevel currentLevel;
    private String levelDisplayName;
    private Integer levelNumber;
    private Integer pointsToNextLevel;
    private Integer dailyEarnedPoints;
    private Integer remainingDailyLimit;
    private LocalDate lastEarnedDate;
    private LocalDateTime createdAt;
}
