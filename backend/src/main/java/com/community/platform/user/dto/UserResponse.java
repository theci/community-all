package com.community.platform.user.dto;

import com.community.platform.authorization.domain.UserRole;
import com.community.platform.user.domain.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 */
@Getter
@Builder
public class UserResponse {

    private Long id; // 사용자 ID

    private String email; // 이메일 주소

    private String nickname; // 닉네임

    private UserRole role; // 사용자 역할

    private UserStatus status; // 사용자 상태

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt; // 생성 시간

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt; // 마지막 로그인 시간

    private UserProfileResponse profile; // 프로필 정보 (선택적)
}