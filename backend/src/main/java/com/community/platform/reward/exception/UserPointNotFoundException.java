package com.community.platform.reward.exception;

/**
 * 사용자 포인트 정보를 찾을 수 없을 때 발생하는 예외
 */
public class UserPointNotFoundException extends RuntimeException {

    public UserPointNotFoundException(Long userId) {
        super("사용자의 포인트 정보를 찾을 수 없습니다. userId: " + userId);
    }

    public UserPointNotFoundException(String message) {
        super(message);
    }
}
