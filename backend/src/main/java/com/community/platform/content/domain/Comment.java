package com.community.platform.content.domain;

import com.community.platform.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments",
       indexes = {
           @Index(name = "idx_comment_post_status_created", columnList = "post_id, status, created_at"),
           @Index(name = "idx_comment_parent_created", columnList = "parent_comment_id, created_at")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends AggregateRoot {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> childComments = new ArrayList<>();

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentStatus status = CommentStatus.ACTIVE;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(nullable = false)
    private Integer depth = 0;

    private Comment(Post post, Comment parentComment, Long authorId, String content) {
        this.post = post;
        this.parentComment = parentComment;
        this.authorId = authorId;
        this.content = content;
        this.status = CommentStatus.ACTIVE;
        this.depth = calculateDepth(parentComment);

        if (parentComment != null) {
            parentComment.childComments.add(this);
        }

        post.increaseCommentCount();
    }

    public static Comment create(Post post, Long authorId, String content) {
        validateContent(content);
        Comment comment = new Comment(post, null, authorId, content);

        // 댓글 생성 이벤트 발행 (알림용)
        comment.addDomainEvent(new CommentCreatedEvent(
                comment.getId(),
                post.getId(),
                post.getAuthorId(),
                authorId,
                null,  // 부모 댓글 없음
                null   // 부모 댓글 작성자 없음
        ));

        return comment;
    }

    public static Comment createReply(Post post, Comment parentComment, Long authorId, String content) {
        validateContent(content);
        validateReplyDepth(parentComment);
        Comment comment = new Comment(post, parentComment, authorId, content);

        // 대댓글 생성 이벤트 발행 (알림용)
        comment.addDomainEvent(new CommentCreatedEvent(
                comment.getId(),
                post.getId(),
                post.getAuthorId(),
                authorId,
                parentComment.getId(),
                parentComment.getAuthorId()
        ));

        return comment;
    }

    public void updateContent(String content) {
        validateContent(content);
        this.content = content;
    }

    public void delete() {
        this.status = CommentStatus.DELETED;
        this.post.decreaseCommentCount();
    }

    public void block() {
        this.status = CommentStatus.BLOCKED;
    }

    public void restore() {
        this.status = CommentStatus.ACTIVE;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public boolean isActive() {
        return this.status == CommentStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return this.status == CommentStatus.DELETED;
    }

    public boolean isBlocked() {
        return this.status == CommentStatus.BLOCKED;
    }

    public boolean isRootComment() {
        return this.parentComment == null;
    }

    public boolean hasReplies() {
        return !this.childComments.isEmpty();
    }

    private Integer calculateDepth(Comment parentComment) {
        return parentComment == null ? 0 : parentComment.getDepth() + 1;
    }

    private static void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다.");
        }
        if (content.length() > 1000) {
            throw new IllegalArgumentException("댓글은 1000자를 초과할 수 없습니다.");
        }
    }

    private static void validateReplyDepth(Comment parentComment) {
        if (parentComment.getDepth() >= 2) {
            throw new IllegalArgumentException("대댓글은 2단계까지만 허용됩니다.");
        }
    }
}