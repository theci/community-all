package com.community.platform.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 댓글 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class CommentUpdateRequest {

    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 1000, message = "댓글은 1000자를 초과할 수 없습니다")
    private String content; // 댓글 내용
}