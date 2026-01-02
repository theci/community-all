package com.community.platform.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 댓글 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class CommentCreateRequest {

    @NotNull(message = "게시글 ID는 필수입니다")
    private Long postId; // 게시글 ID

    private Long parentCommentId; // 부모 댓글 ID (대댓글인 경우)

    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 1000, message = "댓글은 1000자를 초과할 수 없습니다")
    private String content; // 댓글 내용
}