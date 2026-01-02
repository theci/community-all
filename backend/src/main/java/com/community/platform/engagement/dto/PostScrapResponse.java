package com.community.platform.engagement.dto;

import com.community.platform.content.dto.PostSummaryResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 게시글 스크랩 응답 DTO
 */
@Getter
@Builder
public class PostScrapResponse {

    private Long id; // 스크랩 ID

    private Long postId; // 게시글 ID (필수)

    private Long userId; // 사용자 ID (필수)

    private Long folderId; // 폴더 ID (필수)

    private PostSummaryResponse post; // 스크랩된 게시글 정보

    private ScrapFolderResponse scrapFolder; // 스크랩 폴더 정보

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt; // 스크랩한 시간
}