package com.community.platform.engagement.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 좋아요 토글 결과 응답 DTO
 */
@Getter
@Builder
public class LikeToggleResponse {

    private Long postId; // 게시글 ID

    private Boolean isLiked; // 좋아요 상태 (true: 좋아요 추가, false: 좋아요 취소)

    private Long totalLikeCount; // 전체 좋아요 수

    private String message; // 결과 메시지
    
    /**
     * 좋아요 추가 응답 생성
     */
    public static LikeToggleResponse liked(Long totalCount) {
        return LikeToggleResponse.builder()
                .isLiked(true)
                .totalLikeCount(totalCount)
                .message("좋아요를 누르셨습니다")
                .build();
    }

    /**
     * 좋아요 취소 응답 생성
     */
    public static LikeToggleResponse unliked(Long totalCount) {
        return LikeToggleResponse.builder()
                .isLiked(false)
                .totalLikeCount(totalCount)
                .message("좋아요를 취소하셨습니다")
                .build();
    }
}