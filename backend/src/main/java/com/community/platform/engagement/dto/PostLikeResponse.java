package com.community.platform.engagement.dto;

import com.community.platform.user.dto.UserSummaryResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 게시글 좋아요 응답 DTO
 */
@Getter
@Builder
public class PostLikeResponse {

    private Long id; // 좋아요 ID
    
    private Long postId; // 게시글 ID
    
    private UserSummaryResponse user; // 좋아요한 사용자 정보
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt; // 좋아요한 시간
}