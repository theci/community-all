package com.community.platform.user.application;

import com.community.platform.user.domain.User;
import com.community.platform.user.domain.UserProfile;
import com.community.platform.user.dto.UserProfileResponse;
import com.community.platform.user.dto.UserResponse;
import com.community.platform.user.dto.UserSummaryResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-23T11:21:06+0000",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.id( user.getId() );
        userResponse.email( user.getEmail() );
        userResponse.nickname( user.getNickname() );
        userResponse.status( user.getStatus() );
        userResponse.createdAt( user.getCreatedAt() );
        userResponse.lastLoginAt( user.getLastLoginAt() );

        return userResponse.build();
    }

    @Override
    public UserSummaryResponse toUserSummaryResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserSummaryResponse.UserSummaryResponseBuilder userSummaryResponse = UserSummaryResponse.builder();

        userSummaryResponse.id( user.getId() );
        userSummaryResponse.nickname( user.getNickname() );

        return userSummaryResponse.build();
    }

    @Override
    public UserProfileResponse toUserProfileResponse(UserProfile userProfile) {
        if ( userProfile == null ) {
            return null;
        }

        UserProfileResponse.UserProfileResponseBuilder userProfileResponse = UserProfileResponse.builder();

        userProfileResponse.bio( userProfile.getBio() );
        userProfileResponse.birthDate( userProfile.getBirthDate() );
        userProfileResponse.gender( userProfile.getGender() );
        userProfileResponse.phoneNumber( userProfile.getPhoneNumber() );
        userProfileResponse.profileImageUrl( userProfile.getProfileImageUrl() );

        return userProfileResponse.build();
    }

    @Override
    public List<UserResponse> toUserResponseList(List<User> users) {
        if ( users == null ) {
            return null;
        }

        List<UserResponse> list = new ArrayList<UserResponse>( users.size() );
        for ( User user : users ) {
            list.add( toUserResponse( user ) );
        }

        return list;
    }

    @Override
    public List<UserSummaryResponse> toUserSummaryResponseList(List<User> users) {
        if ( users == null ) {
            return null;
        }

        List<UserSummaryResponse> list = new ArrayList<UserSummaryResponse>( users.size() );
        for ( User user : users ) {
            list.add( toUserSummaryResponse( user ) );
        }

        return list;
    }
}
