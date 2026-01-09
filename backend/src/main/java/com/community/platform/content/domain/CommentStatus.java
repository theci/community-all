package com.community.platform.content.domain;

public enum CommentStatus {
    ACTIVE("활성"),
    DELETED("삭제됨"),
    BLOCKED("차단됨"),
    REPORTED("신고 처리됨");

    private final String description;

    CommentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}