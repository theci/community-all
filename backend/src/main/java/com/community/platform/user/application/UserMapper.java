package com.community.platform.user.application;

import com.community.platform.user.domain.User;
import com.community.platform.user.domain.UserProfile;
import com.community.platform.user.dto.UserProfileResponse;
import com.community.platform.user.dto.UserResponse;
import com.community.platform.user.dto.UserSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * User 도메인 객체와 DTO 간 매핑을 담당하는 MapStruct 매퍼
 */
@Mapper(
    componentModel = "spring", // Spring Bean으로 등록
    unmappedTargetPolicy = ReportingPolicy.IGNORE // 매핑되지 않은 필드 무시
)
public interface UserMapper {

    /**
     * User 엔티티를 UserResponse DTO로 변환
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "nickname", target = "nickname")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "lastLoginAt", target = "lastLoginAt")
    @Mapping(target = "profile", ignore = true) // 별도 매핑 필요
    UserResponse toUserResponse(User user);

    /**
     * User 엔티티를 UserSummaryResponse DTO로 변환
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "nickname", target = "nickname")
    @Mapping(target = "profileImageUrl", ignore = true) // UserProfile에서 가져와야 함
    UserSummaryResponse toUserSummaryResponse(User user);

    /**
     * UserProfile 엔티티를 UserProfileResponse DTO로 변환
     */
    UserProfileResponse toUserProfileResponse(UserProfile userProfile);

    /**
     * User 리스트를 UserResponse 리스트로 변환
     */
    List<UserResponse> toUserResponseList(List<User> users);

    /**
     * User 리스트를 UserSummaryResponse 리스트로 변환
     */
    List<UserSummaryResponse> toUserSummaryResponseList(List<User> users);

    /**
     * User와 UserProfile을 함께 사용하여 완전한 UserResponse 생성
     */
    default UserResponse toUserResponseWithProfile(User user, UserProfile profile) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt());

        if (profile != null) {
            builder.profile(toUserProfileResponse(profile));
        }

        return builder.build();
    }

    /**
     * User와 UserProfile을 함께 사용하여 UserSummaryResponse 생성 (프로필 이미지 포함)
     */
    default UserSummaryResponse toUserSummaryResponseWithProfile(User user, UserProfile profile) {
        UserSummaryResponse.UserSummaryResponseBuilder builder = UserSummaryResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname());

        if (profile != null && profile.getProfileImageUrl() != null) {
            builder.profileImageUrl(profile.getProfileImageUrl());
        }

        return builder.build();
    }
}