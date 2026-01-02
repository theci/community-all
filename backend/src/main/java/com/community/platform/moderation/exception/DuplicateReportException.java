package com.community.platform.moderation.exception;

/**
 * 중복 신고 예외
 */
public class DuplicateReportException extends RuntimeException {
    public DuplicateReportException(Long userId, Long targetId) {
        super(String.format("이미 신고한 콘텐츠입니다. (사용자 ID: %d, 대상 ID: %d)", userId, targetId));
    }

    public DuplicateReportException(String message) {
        super(message);
    }
}
