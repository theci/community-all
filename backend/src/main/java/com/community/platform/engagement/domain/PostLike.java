package com.community.platform.engagement.domain;

import com.community.platform.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_likes",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_post_like_user_post", columnNames = {"user_id", "post_id"})
       },
       indexes = {
           @Index(name = "idx_post_like_user_post", columnList = "user_id, post_id"),
           @Index(name = "idx_post_like_post_created", columnList = "post_id, created_at")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends AggregateRoot {

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private PostLike(Long postId, Long userId) {
        this.postId = postId;
        this.userId = userId;
    }

    public static PostLike create(Long postId, Long userId, Long postAuthorId) {
        PostLike like = new PostLike(postId, userId);

        // 좋아요 생성 이벤트 발행 (알림용)
        like.addDomainEvent(new LikeCreatedEvent(
                like.getId(),
                postId,
                postAuthorId,
                userId
        ));

        return like;
    }

    // 하위 호환성을 위한 오버로드 메서드 (기존 코드 호환)
    public static PostLike create(Long postId, Long userId) {
        return new PostLike(postId, userId);
    }
}