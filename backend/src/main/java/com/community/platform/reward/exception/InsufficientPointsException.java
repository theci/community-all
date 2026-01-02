package com.community.platform.reward.exception;

/**
 * 포인트 부족 예외
 */
public class InsufficientPointsException extends RuntimeException {

    public InsufficientPointsException(int required, int available) {
        super(String.format("포인트가 부족합니다. 필요: %d, 보유: %d", required, available));
    }

    public InsufficientPointsException(String message) {
        super(message);
    }
}
