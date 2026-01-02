package com.community.platform.reward.domain;

import com.community.platform.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 사용자 포인트 관리 엔티티
 * 포인트 적립, 차감, 레벨 관리
 */
@Entity
@Table(name = "user_points",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_point_user", columnNames = "user_id")
       },
       indexes = {
           @Index(name = "idx_user_point_level", columnList = "current_level, total_points"),
           @Index(name = "idx_user_point_total", columnList = "total_points DESC")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPoint extends AggregateRoot {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0; // 총 포인트 (누적)

    @Column(name = "available_points", nullable = false)
    private Integer availablePoints = 0; // 사용 가능한 포인트 (현재)

    @Enumerated(EnumType.STRING)
    @Column(name = "current_level", nullable = false, length = 20)
    private UserLevel currentLevel = UserLevel.LEVEL_1;

    @Column(name = "daily_earned_points", nullable = false)
    private Integer dailyEarnedPoints = 0; // 오늘 획득한 포인트

    @Column(name = "last_earned_date")
    private LocalDate lastEarnedDate; // 마지막 포인트 획득 날짜

    private UserPoint(Long userId) {
        this.userId = userId;
        this.totalPoints = 0;
        this.availablePoints = 0;
        this.currentLevel = UserLevel.LEVEL_1;
        this.dailyEarnedPoints = 0;
        this.lastEarnedDate = null;
    }

    /**
     * 사용자 포인트 초기화
     */
    public static UserPoint create(Long userId) {
        return new UserPoint(userId);
    }

    /**
     * 포인트 적립
     */
    public void earnPoints(int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("적립 포인트는 양수여야 합니다.");
        }

        // 일일 한도 체크
        checkDailyLimit(points);

        this.totalPoints += points;
        this.availablePoints += points;

        // 오늘 획득 포인트 업데이트
        updateDailyEarnedPoints(points);

        // 레벨 업데이트
        updateLevel();
    }

    /**
     * 포인트 차감
     */
    public void deductPoints(int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("차감 포인트는 양수여야 합니다.");
        }

        if (this.availablePoints < points) {
            throw new IllegalStateException(
                String.format("포인트가 부족합니다. 현재: %d, 필요: %d",
                             this.availablePoints, points));
        }

        this.availablePoints -= points;
        // totalPoints는 차감하지 않음 (누적 포인트는 유지)
    }

    /**
     * 포인트 사용
     */
    public void usePoints(int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("사용 포인트는 양수여야 합니다.");
        }

        if (this.availablePoints < points) {
            throw new IllegalStateException(
                String.format("포인트가 부족합니다. 현재: %d, 필요: %d",
                             this.availablePoints, points));
        }

        this.availablePoints -= points;
    }

    /**
     * 관리자 포인트 지급 (일일 한도 무시)
     */
    public void adminGrantPoints(int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("지급 포인트는 양수여야 합니다.");
        }

        this.totalPoints += points;
        this.availablePoints += points;
        updateLevel();
    }

    /**
     * 일일 한도 체크
     */
    private void checkDailyLimit(int points) {
        LocalDate today = LocalDate.now();

        // 날짜가 바뀌었으면 일일 포인트 초기화
        if (lastEarnedDate == null || !lastEarnedDate.equals(today)) {
            this.dailyEarnedPoints = 0;
            this.lastEarnedDate = today;
        }

        int dailyLimit = currentLevel.getDailyPointLimit();
        if (this.dailyEarnedPoints + points > dailyLimit) {
            throw new IllegalStateException(
                String.format("일일 포인트 획득 한도를 초과했습니다. (한도: %d, 현재: %d)",
                             dailyLimit, this.dailyEarnedPoints));
        }
    }

    /**
     * 오늘 획득 포인트 업데이트
     */
    private void updateDailyEarnedPoints(int points) {
        LocalDate today = LocalDate.now();

        if (lastEarnedDate == null || !lastEarnedDate.equals(today)) {
            this.dailyEarnedPoints = points;
            this.lastEarnedDate = today;
        } else {
            this.dailyEarnedPoints += points;
        }
    }

    /**
     * 레벨 업데이트
     */
    private void updateLevel() {
        UserLevel newLevel = UserLevel.fromPoints(this.totalPoints);
        if (newLevel != this.currentLevel) {
            UserLevel oldLevel = this.currentLevel;
            this.currentLevel = newLevel;

            // 레벨업 이벤트 발행 (알림용)
            addDomainEvent(new UserLevelUpEvent(
                    this.userId,
                    oldLevel,
                    newLevel,
                    this.totalPoints.longValue()
            ));
        }
    }

    /**
     * 다음 레벨까지 필요한 포인트
     */
    public int getPointsToNextLevel() {
        return currentLevel.getPointsToNextLevel(this.totalPoints);
    }

    /**
     * 오늘 남은 일일 포인트 한도
     */
    public int getRemainingDailyLimit() {
        LocalDate today = LocalDate.now();

        // 날짜가 바뀌었으면 전체 한도 반환
        if (lastEarnedDate == null || !lastEarnedDate.equals(today)) {
            return currentLevel.getDailyPointLimit();
        }

        return currentLevel.getDailyPointLimit() - this.dailyEarnedPoints;
    }
}
