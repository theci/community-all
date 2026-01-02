package com.community.platform.reward.domain;

import lombok.Getter;

/**
 * 포인트 거래 유형
 */
@Getter
public enum PointTransactionType {
    // 획득
    POST_CREATE("게시글 작성", 10, true),
    POST_PUBLISHED("게시글 발행", 5, true),
    COMMENT_CREATE("댓글 작성", 3, true),
    POST_LIKED("게시글 좋아요 받음", 2, true),
    COMMENT_LIKED("댓글 좋아요 받음", 1, true),
    POST_SCRAPPED("게시글 스크랩 받음", 5, true),
    DAILY_LOGIN("일일 로그인", 5, true),
    ADMIN_GRANT("관리자 지급", 0, true), // 관리자가 직접 지급

    // 차감
    POST_DELETE("게시글 삭제", -5, false),
    COMMENT_DELETE("댓글 삭제", -2, false),
    SPAM_PENALTY("스팸 패널티", -50, false),
    REPORT_PENALTY("신고 패널티", -100, false),
    ADMIN_DEDUCT("관리자 차감", 0, false), // 관리자가 직접 차감

    // 사용
    POINT_USE("포인트 사용", 0, false);

    private final String description;
    private final int defaultPoints;
    private final boolean isEarn; // true: 획득, false: 차감/사용

    PointTransactionType(String description, int defaultPoints, boolean isEarn) {
        this.description = description;
        this.defaultPoints = defaultPoints;
        this.isEarn = isEarn;
    }
}
