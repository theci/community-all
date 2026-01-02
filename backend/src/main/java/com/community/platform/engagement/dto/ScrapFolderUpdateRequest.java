package com.community.platform.engagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 스크랩 폴더 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class ScrapFolderUpdateRequest {

    @NotBlank(message = "폴더명은 필수입니다")
    @Size(min = 1, max = 100, message = "폴더명은 1~100자여야 합니다")
    private String name; // 폴더명

    @Size(max = 500, message = "폴더 설명은 500자를 초과할 수 없습니다")
    private String description; // 폴더 설명

    private Boolean isPublic; // 공개 여부 (현재 미구현, 향후 확장용)
}