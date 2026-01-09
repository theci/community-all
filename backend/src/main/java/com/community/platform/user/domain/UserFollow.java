package com.community.platform.user.domain;

import com.community.platform.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 팔로우 관계 엔티티
 */
@Entity
@Table(name = "user_follows",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_follower_following", columnNames = {"follower_id", "following_id"})
       },
       indexes = {
           @Index(name = "idx_follower_id", columnList = "follower_id"),
           @Index(name = "idx_following_id", columnList = "following_id")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFollow extends BaseEntity {

    @Column(name = "follower_id", nullable = false)
    private Long followerId; // 팔로우를 하는 사용자

    @Column(name = "following_id", nullable = false)
    private Long followingId; // 팔로우를 받는 사용자

    private UserFollow(Long followerId, Long followingId) {
        validateFollow(followerId, followingId);
        this.followerId = followerId;
        this.followingId = followingId;
    }

    public static UserFollow create(Long followerId, Long followingId) {
        return new UserFollow(followerId, followingId);
    }

    private void validateFollow(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            throw new IllegalArgumentException("팔로워 ID와 팔로잉 ID는 필수입니다.");
        }
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }
    }
}
