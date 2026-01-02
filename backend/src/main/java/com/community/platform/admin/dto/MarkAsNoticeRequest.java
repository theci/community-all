package com.community.platform.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공지사항 지정/해제 요청 DTO
 */
@Getter
@NoArgsConstructor
public class MarkAsNoticeRequest {

    @NotNull(message = "공지사항 여부는 필수입니다")
    private Boolean isNotice;
}
