package com.community.platform.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림 타입 열거형
 * 사용자에게 발송되는 알림의 종류를 정의
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType {

    COMMENT_ON_POST("내 글에 댓글", "회원님의 게시글에 댓글이 달렸습니다"),
    REPLY_ON_COMMENT("댓글에 답글", "회원님의 댓글에 답글이 달렸습니다"),
    LIKE_ON_POST("게시글 좋아요", "회원님의 게시글을 좋아합니다"),
    SCRAP_ON_POST("게시글 스크랩", "회원님의 게시글을 스크랩했습니다"),
    MESSAGE_RECEIVED("새 쪽지", "새로운 쪽지가 도착했습니다"),
    REPORT_APPROVED("신고 승인", "신고하신 콘텐츠가 삭제되었습니다"),
    REPORT_REJECTED("신고 반려", "신고가 반려되었습니다"),
    PENALTY_APPLIED("제재 통보", "커뮤니티 규칙 위반으로 제재되었습니다"),
    PENALTY_EXPIRED("제재 해제", "제재가 해제되었습니다"),
    LEVEL_UP("레벨 업", "축하합니다! 레벨이 상승했습니다"),
    SYSTEM_NOTICE("시스템 공지", "시스템 공지사항입니다");

    private final String displayName;
    private final String defaultMessage;

    /**
     * 알림 설정에서 ON/OFF 가능한 타입인지 확인
     */
    public boolean isUserConfigurable() {
        // SYSTEM_NOTICE는 항상 발송되므로 설정 불가
        return this != SYSTEM_NOTICE;
    }
}
