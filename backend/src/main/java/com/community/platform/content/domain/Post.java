package com.community.platform.content.domain;

import com.community.platform.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts",
       indexes = {
           @Index(name = "idx_post_author_status_published", columnList = "author_id, status, published_at"),
           @Index(name = "idx_post_category_status_published", columnList = "category_id, status, published_at"),
           @Index(name = "idx_post_status_published_at", columnList = "status, published_at")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends AggregateRoot {

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", length = 20)
    private ContentType contentType = ContentType.MARKDOWN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status = PostStatus.DRAFT;

    @Column(name = "is_notice_post", nullable = false)
    private Boolean isNoticePost = false;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "comment_count", nullable = false)
    private Long commentCount = 0L;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    private Post(Long authorId, Category category, String title, String content, ContentType contentType) {
        this.authorId = authorId;
        this.category = category;
        this.title = title;
        this.content = content;
        this.contentType = contentType;
        this.status = PostStatus.DRAFT;
        
        addDomainEvent(new PostCreatedEvent(this.getId(), authorId, title));
    }

    public static Post create(Long authorId, Category category, String title, String content, ContentType contentType) {
        validateTitle(title);
        validateContent(content);
        return new Post(authorId, category, title, content, contentType);
    }

    public void publish() {
        if (this.status == PostStatus.PUBLISHED) {
            throw new IllegalStateException("이미 발행된 게시글입니다.");
        }
        this.status = PostStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        addDomainEvent(new PostPublishedEvent(this.getId(), this.authorId, this.title));
    }

    public void updateContent(String title, String content) {
        validateTitle(title);
        validateContent(content);
        this.title = title;
        this.content = content;
        addDomainEvent(new PostUpdatedEvent(this.getId(), this.authorId));
    }

    public void delete() {
        delete(null);
    }

    public void delete(Long deletedBy) {
        this.status = PostStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        addDomainEvent(new PostDeletedEvent(this.getId(), this.authorId));
    }

    public void restore() {
        if (!isDeleted()) {
            throw new IllegalStateException("삭제되지 않은 게시글입니다");
        }
        this.status = PostStatus.PUBLISHED;
        this.deletedAt = null;
        this.deletedBy = null;
        addDomainEvent(new PostRestoredEvent(this.getId(), this.authorId));
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public void markAsNotice() {
        this.isNoticePost = true;
        addDomainEvent(new PostMarkedAsNoticeEvent(this.getId()));
    }

    public void unmarkAsNotice() {
        this.isNoticePost = false;
    }

    public boolean isPublished() {
        return this.status == PostStatus.PUBLISHED;
    }

    public boolean isDraft() {
        return this.status == PostStatus.DRAFT;
    }

    public boolean isDeleted() {
        return this.status == PostStatus.DELETED;
    }

    public boolean isNotice() {
        return Boolean.TRUE.equals(this.isNoticePost);
    }

    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (title.length() > 255) {
            throw new IllegalArgumentException("제목은 255자를 초과할 수 없습니다.");
        }
    }

    private static void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }
    }
}