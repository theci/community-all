package com.community.platform.admin.dto;

import com.community.platform.content.domain.PostStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자 게시글 관리 응답 DTO
 */
@Getter
@Builder
public class PostManagementResponse {

    private Long postId;
    private String title;
    private String summary;
    private String authorNickname;
    private Long authorId;
    private String categoryName;
    private PostStatus status;
    private Boolean isNoticePost;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long scrapCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedAt;

    private Long deletedBy;
}
