package com.community.platform.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토큰 갱신 요청 DTO
 */
@Getter
@NoArgsConstructor
public class RefreshTokenRequest {
    
    @NotBlank(message = "리프레시 토큰은 필수입니다")
    private String refreshToken;
    
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}