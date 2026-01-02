package com.community.platform.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO
 */
@Getter
@NoArgsConstructor
public class AuthRequest {
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다")
    private String email;
    
    @NotBlank(message = "패스워드는 필수입니다")
    @Size(min = 8, max = 20, message = "패스워드는 8~20자여야 합니다")
    private String password;
    
    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}