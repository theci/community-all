package com.community.platform.engagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 게시글 스크랩 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class PostScrapRequest {

    @NotNull(message = "게시글 ID는 필수입니다")
    private Long postId; // 게시글 ID

    private Long folderId; // 스크랩 폴더 ID (null이면 기본 폴더)

    // Controller 호환성을 위한 별칭 getter
    public Long getScrapFolderId() {
        return this.folderId;
    }
}