package com.community.platform.reward.domain;

import com.community.platform.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 포인트 거래 내역
 * 포인트 획득/차감/사용의 모든 이력을 추적
 */
@Entity
@Table(name = "point_transactions",
       indexes = {
           @Index(name = "idx_point_transaction_user", columnList = "user_id, created_at"),
           @Index(name = "idx_point_transaction_type", columnList = "transaction_type, created_at")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private PointTransactionType transactionType;

    @Column(nullable = false)
    private Integer points; // 증감 포인트 (양수: 획득, 음수: 차감)

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter; // 거래 후 잔액

    @Column(name = "reference_id")
    private Long referenceId; // 관련 엔티티 ID (게시글 ID, 댓글 ID 등)

    @Column(name = "reference_type", length = 50)
    private String referenceType; // 관련 엔티티 타입 (POST, COMMENT 등)

    @Column(length = 500)
    private String description; // 거래 상세 설명

    @Column(name = "admin_id")
    private Long adminId; // 관리자 지급/차감인 경우 관리자 ID

    private PointTransaction(Long userId, PointTransactionType transactionType,
                            Integer points, Integer balanceAfter,
                            Long referenceId, String referenceType,
                            String description, Long adminId) {
        this.userId = userId;
        this.transactionType = transactionType;
        this.points = points;
        this.balanceAfter = balanceAfter;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.description = description;
        this.adminId = adminId;
    }

    /**
     * 포인트 획득 거래 생성
     */
    public static PointTransaction earn(Long userId, PointTransactionType type,
                                       Integer points, Integer balanceAfter,
                                       Long referenceId, String referenceType) {
        if (!type.isEarn()) {
            throw new IllegalArgumentException("획득 타입이 아닙니다: " + type);
        }
        return new PointTransaction(userId, type, Math.abs(points), balanceAfter,
                                   referenceId, referenceType, type.getDescription(), null);
    }

    /**
     * 포인트 차감 거래 생성
     */
    public static PointTransaction deduct(Long userId, PointTransactionType type,
                                         Integer points, Integer balanceAfter,
                                         Long referenceId, String referenceType) {
        if (type.isEarn()) {
            throw new IllegalArgumentException("차감 타입이 아닙니다: " + type);
        }
        return new PointTransaction(userId, type, -Math.abs(points), balanceAfter,
                                   referenceId, referenceType, type.getDescription(), null);
    }

    /**
     * 관리자 포인트 지급/차감 거래 생성
     */
    public static PointTransaction admin(Long userId, Long adminId,
                                        Integer points, Integer balanceAfter,
                                        String description) {
        PointTransactionType type = points > 0 ?
            PointTransactionType.ADMIN_GRANT : PointTransactionType.ADMIN_DEDUCT;
        return new PointTransaction(userId, type, points, balanceAfter,
                                   null, null, description, adminId);
    }

    /**
     * 포인트 사용 거래 생성
     */
    public static PointTransaction use(Long userId, Integer points, Integer balanceAfter,
                                      String description) {
        return new PointTransaction(userId, PointTransactionType.POINT_USE,
                                   -Math.abs(points), balanceAfter,
                                   null, null, description, null);
    }
}
