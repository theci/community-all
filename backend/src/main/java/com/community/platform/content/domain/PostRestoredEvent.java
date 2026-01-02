package com.community.platform.content.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 게시글 복구 도메인 이벤트
 */
@Getter
@RequiredArgsConstructor
public class PostRestoredEvent implements DomainEvent {

    private final Long postId;
    private final Long authorId;
    private final LocalDateTime occurredOn = LocalDateTime.now();
}
