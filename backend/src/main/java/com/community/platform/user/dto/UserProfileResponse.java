package com.community.platform.user.dto;

import com.community.platform.user.domain.UserProfile;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 사용자 프로필 응답 DTO
 */
@Getter
@Builder
public class UserProfileResponse {

    private String profileImageUrl; // 프로필 이미지 URL
    
    private String bio; // 자기소개
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate; // 생년월일
    
    private UserProfile.Gender gender; // 성별
    
    private String phoneNumber; // 전화번호
}