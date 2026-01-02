package com.community.platform.user.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 인증 성공 응답 DTO
 */
@Getter
@Builder
public class AuthResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserResponse user;
    
    public static AuthResponse of(String accessToken, String refreshToken, String tokenType, 
                                  Long expiresIn, UserResponse user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .expiresIn(expiresIn)
                .user(user)
                .build();
    }
}