package com.community.platform.admin.dto;

import com.community.platform.content.domain.PostStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글 상태 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class UpdatePostStatusRequest {

    @NotNull(message = "상태는 필수입니다")
    private PostStatus status;

    private String reason; // 삭제/복구 사유
}
