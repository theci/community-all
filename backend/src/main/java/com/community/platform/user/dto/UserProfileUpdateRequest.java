package com.community.platform.user.dto;

import com.community.platform.user.domain.UserProfile;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 사용자 프로필 업데이트 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class UserProfileUpdateRequest {

    @Size(max = 500, message = "프로필 이미지 URL은 500자를 초과할 수 없습니다")
    private String profileImageUrl; // 프로필 이미지 URL

    @Size(max = 1000, message = "자기소개는 1000자를 초과할 수 없습니다")
    private String bio; // 자기소개

    @Past(message = "생년월일은 과거 날짜여야 합니다")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate; // 생년월일

    private UserProfile.Gender gender; // 성별
}