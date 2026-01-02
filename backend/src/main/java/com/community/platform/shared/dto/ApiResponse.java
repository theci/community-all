package com.community.platform.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 공통 API 응답 형식
 * 성공/실패 상관없이 일관된 응답 구조 제공
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private final boolean success; // 성공 여부
    private final T data; // 응답 데이터
    private final String message; // 메시지
    private final String errorCode; // 에러 코드 (실패 시)
    private final LocalDateTime timestamp; // 응답 시간

    private ApiResponse(boolean success, T data, String message, String errorCode) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 성공 응답 생성 (데이터 포함)
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    /**
     * 성공 응답 생성 (메시지만)
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, null, message, null);
    }

    /**
     * 성공 응답 생성 (데이터 + 메시지)
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    /**
     * 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, null, message, errorCode);
    }

    /**
     * 실패 응답 생성 (에러 코드만)
     */
    public static <T> ApiResponse<T> error(String errorCode) {
        return new ApiResponse<>(false, null, null, errorCode);
    }
}