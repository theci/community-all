package com.community.platform.content.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 댓글 작성 도메인 이벤트
 * 알림 시스템에서 사용
 */
@Getter
@RequiredArgsConstructor
public class CommentCreatedEvent implements DomainEvent {

    private final Long commentId;
    private final Long postId;
    private final Long postAuthorId;  // 게시글 작성자 ID
    private final Long commentAuthorId;  // 댓글 작성자 ID
    private final Long parentCommentId;  // 부모 댓글 ID (대댓글인 경우)
    private final Long parentCommentAuthorId;  // 부모 댓글 작성자 ID (대댓글인 경우)
    private final LocalDateTime occurredOn = LocalDateTime.now();
}
