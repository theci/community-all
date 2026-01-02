package com.community.platform.moderation.exception;

/**
 * 신고를 찾을 수 없을 때 발생하는 예외
 */
public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(Long reportId) {
        super(String.format("신고를 찾을 수 없습니다. (ID: %d)", reportId));
    }

    public ReportNotFoundException(String message) {
        super(message);
    }
}
