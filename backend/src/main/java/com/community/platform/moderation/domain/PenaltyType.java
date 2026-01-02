package com.community.platform.moderation.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

/**
 * 제재 타입
 * 사용자에게 부여할 수 있는 제재의 종류와 기간 정의
 */
@Getter
@RequiredArgsConstructor
public enum PenaltyType {

    // 글쓰기 금지
    POST_BAN_24H("글쓰기 금지 24시간", Duration.ofHours(24)),
    POST_BAN_7D("글쓰기 금지 7일", Duration.ofDays(7)),
    POST_BAN_PERMANENT("글쓰기 영구 금지", null),

    // 댓글 금지
    COMMENT_BAN_24H("댓글 금지 24시간", Duration.ofHours(24)),
    COMMENT_BAN_7D("댓글 금지 7일", Duration.ofDays(7)),
    COMMENT_BAN_PERMANENT("댓글 영구 금지", null),

    // 계정 정지
    FULL_BAN("계정 정지", null);

    private final String displayName;
    private final Duration duration;  // null이면 영구 제재

    /**
     * 영구 제재 여부
     */
    public boolean isPermanent() {
        return duration == null;
    }

    /**
     * 글쓰기 금지 제재 여부
     */
    public boolean isPostBan() {
        return this == POST_BAN_24H || this == POST_BAN_7D || this == POST_BAN_PERMANENT;
    }

    /**
     * 댓글 금지 제재 여부
     */
    public boolean isCommentBan() {
        return this == COMMENT_BAN_24H || this == COMMENT_BAN_7D || this == COMMENT_BAN_PERMANENT;
    }

    /**
     * 전체 계정 정지 여부
     */
    public boolean isFullBan() {
        return this == FULL_BAN;
    }
}
