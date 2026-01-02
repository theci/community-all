package com.community.platform.content.dto;

import com.community.platform.user.dto.UserSummaryResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 요약 정보 응답 DTO
 * 게시글 목록 표시용 (상세 내용 제외)
 */
@Getter
@Builder
public class PostSummaryResponse {

    private Long id; // 게시글 ID
    
    private UserSummaryResponse author; // 작성자 정보
    
    private CategoryResponse category; // 카테고리 정보
    
    private String title; // 제목
    
    private String summary; // 내용 요약 (앞 100자 정도)
    
    private Boolean isNoticePost; // 공지사항 여부
    
    private Long viewCount; // 조회수
    
    private Long likeCount; // 좋아요 수
    
    private Long commentCount; // 댓글 수
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt; // 발행 시간
    
    private List<TagResponse> tags; // 태그 목록 (최대 3개)
    
    // 사용자별 상호작용 정보
    private Boolean isLikedByUser; // 사용자 좋아요 여부
    
    private Boolean isScrappedByUser; // 사용자 스크랩 여부
}