package com.community.platform.content.domain;

import com.community.platform.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_tags",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_post_tag", columnNames = {"post_id", "tag_id"})
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostTag extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    private PostTag(Post post, Tag tag) {
        this.post = post;
        this.tag = tag;
        tag.increaseUsageCount();
    }

    public static PostTag create(Post post, Tag tag) {
        return new PostTag(post, tag);
    }

    public void remove() {
        this.tag.decreaseUsageCount();
    }
}