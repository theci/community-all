package com.community.platform.user.domain;

public enum UserStatus {
    ACTIVE("활성"),
    INACTIVE("비활성"), 
    BLOCKED("차단됨");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}