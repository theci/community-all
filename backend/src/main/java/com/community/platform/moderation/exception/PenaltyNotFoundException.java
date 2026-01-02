package com.community.platform.moderation.exception;

/**
 * 제재 정보를 찾을 수 없을 때 발생하는 예외
 */
public class PenaltyNotFoundException extends RuntimeException {

    public PenaltyNotFoundException(Long penaltyId) {
        super("제재 정보를 찾을 수 없습니다. ID: " + penaltyId);
    }

    public PenaltyNotFoundException(String message) {
        super(message);
    }
}
