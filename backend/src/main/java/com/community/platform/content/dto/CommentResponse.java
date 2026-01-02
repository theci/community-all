package com.community.platform.content.dto;

import com.community.platform.content.domain.CommentStatus;
import com.community.platform.user.dto.UserSummaryResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 댓글 응답 DTO
 */
@Getter
@Builder
public class CommentResponse {

    private Long id; // 댓글 ID
    
    private Long postId; // 게시글 ID
    
    private Long parentCommentId; // 부모 댓글 ID (대댓글인 경우)
    
    private UserSummaryResponse author; // 작성자 정보
    
    private String content; // 댓글 내용
    
    private CommentStatus status; // 댓글 상태
    
    private Long likeCount; // 좋아요 수
    
    private Integer depth; // 댓글 깊이 (0: 최상위, 1: 대댓글)
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt; // 생성 시간
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt; // 수정 시간
    
    private List<CommentResponse> replies; // 대댓글 목록
    
    // 사용자별 상호작용 정보
    private Boolean isLikedByUser; // 사용자 좋아요 여부
    
    private Boolean isAuthor; // 작성자 여부 (수정/삭제 권한 표시용)
}