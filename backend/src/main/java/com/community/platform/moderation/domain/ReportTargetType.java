package com.community.platform.moderation.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 신고 대상 유형
 */
@Getter
@RequiredArgsConstructor
public enum ReportTargetType {
    POST("게시글", "부적절한 게시글을 신고합니다"),
    COMMENT("댓글", "부적절한 댓글을 신고합니다"),
    CHAT("채팅", "부적절한 채팅 메시지를 신고합니다"),
    USER("사용자", "부적절한 행동을 하는 사용자를 신고합니다");

    private final String displayName;
    private final String description;

    public static ReportTargetType fromString(String value) {
        for (ReportTargetType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 신고 대상 유형입니다: " + value);
    }
}
