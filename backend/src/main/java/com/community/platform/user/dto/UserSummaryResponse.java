package com.community.platform.user.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 요약 정보 응답 DTO
 * 게시글 작성자 표시, 댓글 작성자 표시 등에 사용
 */
@Getter
@Builder
public class UserSummaryResponse {

    private Long id; // 사용자 ID
    
    private String nickname; // 닉네임
    
    private String profileImageUrl; // 프로필 이미지 URL
}