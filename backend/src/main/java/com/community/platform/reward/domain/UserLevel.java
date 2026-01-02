package com.community.platform.reward.domain;

import lombok.Getter;

/**
 * 사용자 레벨 정의
 * 포인트 구간에 따라 레벨이 결정됨
 */
@Getter
public enum UserLevel {
    LEVEL_1("새싹", 0, 99, "커뮤니티에 처음 오신 분들"),
    LEVEL_2("일반", 100, 499, "활동을 시작한 회원"),
    LEVEL_3("단골", 500, 999, "꾸준히 활동하는 회원"),
    LEVEL_4("열성", 1000, 1999, "열정적으로 활동하는 회원"),
    LEVEL_5("고수", 2000, 3999, "커뮤니티의 중심 멤버"),
    LEVEL_6("달인", 4000, 7999, "뛰어난 활동으로 인정받는 회원"),
    LEVEL_7("명인", 8000, 15999, "커뮤니티를 이끄는 리더"),
    LEVEL_8("전설", 16000, 31999, "전설적인 기여를 한 회원"),
    LEVEL_9("영웅", 32000, 63999, "커뮤니티의 영웅"),
    LEVEL_10("신화", 64000, Integer.MAX_VALUE, "커뮤니티의 신화");

    private final String displayName;
    private final int minPoints;
    private final int maxPoints;
    private final String description;

    UserLevel(String displayName, int minPoints, int maxPoints, String description) {
        this.displayName = displayName;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.description = description;
    }

    /**
     * 포인트에 따른 레벨 결정
     */
    public static UserLevel fromPoints(int points) {
        for (UserLevel level : values()) {
            if (points >= level.minPoints && points <= level.maxPoints) {
                return level;
            }
        }
        return LEVEL_1;
    }

    /**
     * 레벨 숫자 반환 (1~10)
     */
    public int getLevelNumber() {
        return this.ordinal() + 1;
    }

    /**
     * 다음 레벨까지 필요한 포인트
     */
    public int getPointsToNextLevel(int currentPoints) {
        if (this == LEVEL_10) {
            return 0; // 최고 레벨
        }
        return this.maxPoints + 1 - currentPoints;
    }

    /**
     * 레벨에 따른 일일 포인트 획득 한도
     */
    public int getDailyPointLimit() {
        return switch (this) {
            case LEVEL_1, LEVEL_2 -> 100;
            case LEVEL_3, LEVEL_4 -> 200;
            case LEVEL_5, LEVEL_6 -> 300;
            case LEVEL_7, LEVEL_8 -> 500;
            case LEVEL_9, LEVEL_10 -> 1000;
        };
    }
}
