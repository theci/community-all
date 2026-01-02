package com.community.platform.reward.exception;

/**
 * 일일 포인트 한도 초과 예외
 */
public class DailyPointLimitExceededException extends RuntimeException {

    public DailyPointLimitExceededException(int limit, int current) {
        super(String.format("일일 포인트 획득 한도를 초과했습니다. 한도: %d, 현재: %d", limit, current));
    }

    public DailyPointLimitExceededException(String message) {
        super(message);
    }
}
