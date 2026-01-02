package com.community.platform.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 게시글 일괄 처리 요청 DTO
 */
@Getter
@NoArgsConstructor
public class BulkPostActionRequest {

    @NotEmpty(message = "게시글 ID 목록은 필수입니다")
    private List<Long> postIds;

    @NotNull(message = "액션 타입은 필수입니다")
    private BulkActionType action;

    private String reason; // 삭제 사유 등

    public enum BulkActionType {
        DELETE,         // 삭제
        RESTORE,        // 복구
        MARK_AS_NOTICE, // 공지사항 지정
        UNMARK_AS_NOTICE // 공지사항 해제
    }
}
