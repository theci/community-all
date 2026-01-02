package com.community.platform.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 회원가입 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class UserRegisterRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다")
    private String email; // 이메일 주소

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
             message = "비밀번호는 대소문자, 숫자, 특수문자(@$!%*?&)를 포함한 8~20자여야 합니다")
    private String password; // 비밀번호

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 50, message = "닉네임은 2~50자여야 합니다")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9_-]+$", 
             message = "닉네임은 한글, 영문, 숫자, _, -만 사용 가능합니다")
    private String nickname; // 닉네임
}