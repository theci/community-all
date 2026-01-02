package com.community.platform.reward.dto;

import com.community.platform.reward.domain.PointTransactionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 포인트 거래 내역 응답 DTO
 */
@Getter
@Builder
public class PointTransactionResponse {
    private Long id;
    private Long userId;
    private PointTransactionType transactionType;
    private String transactionTypeDescription;
    private Integer points;
    private Integer balanceAfter;
    private Long referenceId;
    private String referenceType;
    private String description;
    private Long adminId;
    private LocalDateTime createdAt;
}
