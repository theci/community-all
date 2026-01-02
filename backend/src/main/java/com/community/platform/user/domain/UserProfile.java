package com.community.platform.user.domain;

import com.community.platform.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    private UserProfile(User user) {
        this.user = user;
    }

    public static UserProfile createFor(User user) {
        return new UserProfile(user);
    }

    public void updateProfile(String profileImageUrl, String bio, LocalDate birthDate, Gender gender) {
        this.profileImageUrl = profileImageUrl;
        this.bio = bio;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public void updatePhoneNumber(String phoneNumber) {
        validatePhoneNumber(phoneNumber);
        this.phoneNumber = phoneNumber;
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.matches("^[0-9-+()\\s]+$")) {
            throw new IllegalArgumentException("올바른 전화번호 형식이 아닙니다.");
        }
    }

    public enum Gender {
        MALE("남성"),
        FEMALE("여성"),
        OTHER("기타");

        private final String description;

        Gender(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}