package com.community.platform.moderation.exception;

/**
 * 사용자 제재 예외
 * 사용자가 제재 상태일 때 특정 행동을 시도할 경우 발생
 */
public class UserPenaltyException extends RuntimeException {

    public UserPenaltyException(String message) {
        super(message);
    }

    public UserPenaltyException(String message, Throwable cause) {
        super(message, cause);
    }
}
