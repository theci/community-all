package com.community.platform.content.dto;

import com.community.platform.content.domain.ContentType;
import com.community.platform.content.domain.PostStatus;
import com.community.platform.user.dto.UserSummaryResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 응답 DTO
 */
@Getter
@Builder
public class PostResponse {

    private Long id; // 게시글 ID
    
    private UserSummaryResponse author; // 작성자 정보
    
    private CategoryResponse category; // 카테고리 정보
    
    private String title; // 제목
    
    private String content; // 내용
    
    private ContentType contentType; // 콘텐츠 타입
    
    private PostStatus status; // 게시글 상태
    
    private Boolean isNoticePost; // 공지사항 여부
    
    private Long viewCount; // 조회수
    
    private Long likeCount; // 좋아요 수
    
    private Long commentCount; // 댓글 수
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt; // 생성 시간
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt; // 수정 시간
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt; // 발행 시간
    
    private List<TagResponse> tags; // 태그 목록
    
    // 사용자별 상호작용 정보 (로그인한 사용자의 경우)
    private Boolean isLikedByUser; // 사용자 좋아요 여부
    
    private Boolean isScrappedByUser; // 사용자 스크랩 여부
}