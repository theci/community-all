package com.community.platform.shared.infrastructure;

import com.community.platform.shared.domain.BusinessException;
import com.community.platform.shared.dto.ApiResponse;
import com.community.platform.shared.dto.ValidationErrorResponse;
// User domain exceptions
import com.community.platform.user.exception.*;
// Content domain exceptions  
import com.community.platform.content.exception.*;
// Engagement domain exceptions
import com.community.platform.engagement.exception.*;
// Security exceptions
import com.community.platform.shared.security.SecurityException;
import com.community.platform.shared.security.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 * 모든 Controller에서 발생하는 예외를 일관된 형태로 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리 (도메인에서 발생하는 예외)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("비즈니스 예외 발생: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Bean Validation 예외 처리 (@Valid 실패)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ValidationErrorResponse>> handleValidationException(
            MethodArgumentNotValidException e) {
        log.warn("유효성 검증 실패: {}", e.getMessage());

        List<ValidationErrorResponse.FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapToFieldError)
                .collect(Collectors.toList());

        ValidationErrorResponse errorResponse = ValidationErrorResponse.builder()
                .message("입력 값이 올바르지 않습니다")
                .fieldErrors(fieldErrors)
                .build();

        ApiResponse<ValidationErrorResponse> response = ApiResponse.error("유효성 검증 실패", "VALIDATION_ERROR");
        return ResponseEntity.badRequest().body(ApiResponse.success(errorResponse));
    }

    /**
     * Model Binding 예외 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<ValidationErrorResponse>> handleBindException(BindException e) {
        log.warn("바인딩 오류: {}", e.getMessage());

        List<ValidationErrorResponse.FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapToFieldError)
                .collect(Collectors.toList());

        ValidationErrorResponse errorResponse = ValidationErrorResponse.builder()
                .message("요청 데이터 형식이 올바르지 않습니다")
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(ApiResponse.success(errorResponse));
    }

    /**
     * 타입 불일치 예외 처리 (PathVariable, RequestParam 타입 오류)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("타입 불일치 오류: {} = {}", e.getName(), e.getValue());
        
        String message = String.format("'%s' 파라미터 값이 올바르지 않습니다: %s", e.getName(), e.getValue());
        ApiResponse<Void> response = ApiResponse.error(message, "INVALID_PARAMETER");
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * IllegalArgumentException 처리 (일반적인 잘못된 인수)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("잘못된 인수: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), "INVALID_ARGUMENT");
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * IllegalStateException 처리 (잘못된 상태)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException e) {
        log.warn("잘못된 상태: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), "INVALID_STATE");
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 일반적인 예외 처리 (예상하지 못한 서버 오류)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("예상하지 못한 서버 오류 발생", e);
        
        ApiResponse<Void> response = ApiResponse.error(
            "서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", 
            "INTERNAL_SERVER_ERROR"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ========== User Domain Exceptions ==========

    /**
     * 사용자를 찾을 수 없는 예외 처리
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException e) {
        log.warn("사용자 조회 실패: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 중복 사용자 예외 처리
     */
    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateUserException(DuplicateUserException e) {
        log.warn("사용자 중복 오류: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * 인증 실패 예외 처리
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentialsException(InvalidCredentialsException e) {
        log.warn("인증 실패: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 사용자 상태 예외 처리 (차단, 비활성화 등)
     */
    @ExceptionHandler(UserStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserStatusException(UserStatusException e) {
        log.warn("사용자 상태 오류: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ========== Content Domain Exceptions ==========

    /**
     * 게시글을 찾을 수 없는 예외 처리
     */
    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePostNotFoundException(PostNotFoundException e) {
        log.warn("게시글 조회 실패: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 댓글을 찾을 수 없는 예외 처리
     */
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCommentNotFoundException(CommentNotFoundException e) {
        log.warn("댓글 조회 실패: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 카테고리를 찾을 수 없는 예외 처리
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryNotFoundException(CategoryNotFoundException e) {
        log.warn("카테고리 조회 실패: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 게시글 상태 예외 처리
     */
    @ExceptionHandler(InvalidPostStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPostStatusException(InvalidPostStatusException e) {
        log.warn("게시글 상태 오류: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * 권한 없는 접근 예외 처리
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedAccessException(UnauthorizedAccessException e) {
        log.warn("권한 없는 접근: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ========== Engagement Domain Exceptions ==========

    /**
     * 스크랩 폴더를 찾을 수 없는 예외 처리
     */
    @ExceptionHandler(ScrapFolderNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleScrapFolderNotFoundException(ScrapFolderNotFoundException e) {
        log.warn("스크랩 폴더 조회 실패: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 중복 좋아요 예외 처리
     */
    @ExceptionHandler(DuplicateLikeException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateLikeException(DuplicateLikeException e) {
        log.warn("중복 좋아요: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * 중복 스크랩 예외 처리
     */
    @ExceptionHandler(DuplicateScrapException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateScrapException(DuplicateScrapException e) {
        log.warn("중복 스크랩: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * 폴더 제한 초과 예외 처리
     */
    @ExceptionHandler(FolderLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleFolderLimitExceededException(FolderLimitExceededException e) {
        log.warn("폴더 제한 초과: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ========== Security Exceptions ==========

    /**
     * Spring Security 인증 예외 처리
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        log.warn("인증 실패: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("인증이 필요합니다", "AUTHENTICATION_REQUIRED");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Spring Security 권한 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("접근 권한 없음: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("접근 권한이 없습니다", "ACCESS_DENIED");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * JWT 관련 예외 처리
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtException(JwtException e) {
        log.warn("JWT 오류: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 커스텀 보안 예외 처리
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Void>> handleSecurityException(SecurityException e) {
        log.warn("보안 오류: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * FieldError를 ValidationErrorResponse.FieldError로 변환
     */
    private ValidationErrorResponse.FieldError mapToFieldError(FieldError fieldError) {
        return ValidationErrorResponse.FieldError.builder()
                .field(fieldError.getField())
                .rejectedValue(fieldError.getRejectedValue())
                .message(fieldError.getDefaultMessage())
                .build();
    }
}