package com.community.platform.content.domain;

import com.community.platform.shared.domain.DomainEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 첨부파일 업로드 도메인 이벤트
 */
@Getter
@RequiredArgsConstructor
public class PostAttachmentCreatedEvent implements DomainEvent {

    private final Long attachmentId;
    private final Long postId;
    private final AttachmentType fileType;
    private final String originalName;
    private final LocalDateTime occurredOn = LocalDateTime.now();
}
