package com.community.platform.shared.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 유효성 검사 오류 응답 DTO
 */
@Getter
@Builder
public class ValidationErrorResponse {

    private String message; // 전체 오류 메시지
    
    private List<FieldError> fieldErrors; // 필드별 오류 목록

    /**
     * 필드 오류 정보를 담는 내부 클래스
     */
    @Getter
    @Builder
    public static class FieldError {
        private String field; // 필드명
        private Object rejectedValue; // 거부된 값
        private String message; // 오류 메시지
    }
}