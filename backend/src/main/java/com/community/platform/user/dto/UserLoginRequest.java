package com.community.platform.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 로그인 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class UserLoginRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email; // 이메일 주소

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password; // 비밀번호
}