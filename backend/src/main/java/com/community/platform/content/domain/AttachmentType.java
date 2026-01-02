package com.community.platform.content.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * 첨부파일 타입 열거형
 * 이미지와 동영상 타입별로 허용 확장자와 최대 크기 정의
 */
@Getter
@RequiredArgsConstructor
public enum AttachmentType {

    IMAGE("이미지",
            Arrays.asList("jpg", "jpeg", "png", "gif", "webp"),
            10 * 1024 * 1024L),  // 10MB

    VIDEO("동영상",
            Arrays.asList("mp4", "avi", "mov", "wmv"),
            100 * 1024 * 1024L);  // 100MB

    private final String displayName;
    private final List<String> allowedExtensions;
    private final Long maxFileSize;

    /**
     * 파일 확장자로부터 AttachmentType 추론
     */
    public static AttachmentType fromExtension(String extension) {
        String ext = extension.toLowerCase();

        for (AttachmentType type : values()) {
            if (type.getAllowedExtensions().contains(ext)) {
                return type;
            }
        }

        throw new IllegalArgumentException(
                String.format("지원하지 않는 파일 형식입니다: %s", extension)
        );
    }

    /**
     * 파일 크기 검증
     */
    public boolean isValidSize(Long fileSize) {
        return fileSize != null && fileSize <= this.maxFileSize;
    }

    /**
     * 확장자 검증
     */
    public boolean isValidExtension(String extension) {
        return allowedExtensions.contains(extension.toLowerCase());
    }
}
