package com.community.platform.content.domain;

public enum ContentType {
    TEXT("텍스트"),
    MARKDOWN("마크다운"),
    HTML("HTML");

    private final String description;

    ContentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}