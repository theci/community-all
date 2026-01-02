package com.community.platform.authorization.domain;

import lombok.Getter;

/**
 * 세부 권한 (Permission)
 * 각 역할이 수행할 수 있는 구체적인 행동
 */
@Getter
public enum Permission {
    // 특수 권한
    ALL("모든 권한", "시스템의 모든 기능에 대한 권한"),

    // 게시글 관련 권한
    POST_READ("게시글 읽기", "게시글 조회 권한"),
    POST_CREATE("게시글 작성", "게시글 작성 권한"),
    POST_UPDATE_OWN("내 게시글 수정", "자신의 게시글 수정 권한"),
    POST_DELETE_OWN("내 게시글 삭제", "자신의 게시글 삭제 권한"),
    POST_UPDATE_ALL("모든 게시글 수정", "모든 게시글 수정 권한 (관리자)"),
    POST_DELETE_ALL("모든 게시글 삭제", "모든 게시글 삭제 권한 (관리자)"),
    POST_PIN("게시글 고정", "게시글 상단 고정 권한"),
    POST_RECOMMEND("게시글 추천", "게시글 추천/큐레이션 제안 권한"),

    // 댓글 관련 권한
    COMMENT_READ("댓글 읽기", "댓글 조회 권한"),
    COMMENT_CREATE("댓글 작성", "댓글 작성 권한"),
    COMMENT_UPDATE_OWN("내 댓글 수정", "자신의 댓글 수정 권한"),
    COMMENT_DELETE_OWN("내 댓글 삭제", "자신의 댓글 삭제 권한"),
    COMMENT_UPDATE_ALL("모든 댓글 수정", "모든 댓글 수정 권한 (관리자)"),
    COMMENT_DELETE_ALL("모든 댓글 삭제", "모든 댓글 삭제 권한 (관리자)"),

    // 참여 관련 권한
    LIKE_CREATE("좋아요", "좋아요 추가/취소 권한"),
    SCRAP_CREATE("스크랩", "스크랩 추가/취소 권한"),

    // 신고 관련 권한
    REPORT_CREATE("신고 접수", "콘텐츠/사용자 신고 권한"),
    REPORT_MANAGE("신고 관리", "신고 처리 및 관리 권한"),

    // 사용자 관련 권한
    USER_MANAGE("사용자 관리", "사용자 정보 관리 권한"),
    USER_BLOCK("사용자 차단", "사용자 차단/정지 권한"),
    USER_TEMP_BLOCK("임시 차단", "사용자 임시 차단 권한 (파워유저)"),

    // 역할 및 권한 관리
    ROLE_ASSIGN("역할 부여", "사용자 역할 변경 권한"),

    // 포인트 관리
    POINT_MANAGE("포인트 관리", "포인트 지급/차감 권한"),

    // 콘텐츠 필터링
    CONTENT_FILTER_VIEW("필터 조회", "콘텐츠 필터 조회 권한"),
    CONTENT_FILTER_MANAGE("필터 관리", "콘텐츠 필터 설정/관리 권한"),

    // 공지사항 및 큐레이션
    NOTICE_MANAGE("공지사항 관리", "공지사항 작성/관리 권한"),

    // 카테고리 및 태그
    CATEGORY_MANAGE("카테고리 관리", "카테고리 생성/수정/삭제 권한"),

    // 통계 및 분석
    STATISTICS_VIEW("통계 조회", "시스템 통계 조회 권한");

    private final String displayName;
    private final String description;

    Permission(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Spring Security Authority 문자열 반환
     */
    public String getAuthority() {
        return "PERMISSION_" + this.name();
    }
}
