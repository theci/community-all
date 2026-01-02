package com.community.platform.content.dto;

import com.community.platform.content.domain.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 게시글 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class PostCreateRequest {

    @NotNull(message = "카테고리는 필수입니다")
    private Long categoryId; // 카테고리 ID

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다")
    private String title; // 제목

    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 10000, message = "내용은 10000자를 초과할 수 없습니다")
    private String content; // 내용

    private ContentType contentType = ContentType.MARKDOWN; // 콘텐츠 타입 (기본값: MARKDOWN)

    @Size(max = 10, message = "태그는 최대 10개까지 가능합니다")
    private List<@Size(max = 50, message = "태그는 50자를 초과할 수 없습니다") String> tags; // 태그 목록
}