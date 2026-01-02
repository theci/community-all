package com.community.platform.engagement.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 스크랩 생성 도메인 이벤트
 * 알림 시스템에서 사용
 */
@Getter
@RequiredArgsConstructor
public class ScrapCreatedEvent implements DomainEvent {

    private final Long scrapId;
    private final Long postId;
    private final Long postAuthorId;  // 게시글 작성자 ID
    private final Long userId;  // 스크랩한 사용자 ID
    private final LocalDateTime occurredOn = LocalDateTime.now();
}
