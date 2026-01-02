package com.community.platform.moderation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 제재 확인 응답 DTO
 * 사용자가 특정 행동을 할 수 있는지 여부 응답
 */
@Getter
@Builder
@AllArgsConstructor
public class PenaltyCheckResponse {

    private Long userId;
    private Boolean canPost;
    private Boolean canComment;
    private String message;

    public static PenaltyCheckResponse of(Long userId, Boolean canPost, Boolean canComment) {
        String message = canPost && canComment
                ? "제재가 없습니다"
                : "제재 중입니다";

        return PenaltyCheckResponse.builder()
                .userId(userId)
                .canPost(canPost)
                .canComment(canComment)
                .message(message)
                .build();
    }
}
