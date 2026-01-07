package com.community.platform.notification.domain;

/**
 * 알림과 연관된 엔티티 타입
 * 알림 클릭 시 이동할 페이지를 결정하는데 사용
 */
public enum RelatedEntityType {

    POST,       // 게시글
    COMMENT,    // 댓글
    MESSAGE,    // 쪽지
    REPORT,     // 신고
    PENALTY,    // 제재
    USER,       // 사용자
    SYSTEM      // 시스템
}
