package com.community.platform.messaging.dto;

import com.community.platform.user.dto.UserSummaryResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 메시지 스레드(대화방) 응답 DTO
 */
@Getter
@Builder
public class MessageThreadResponse {

    private Long id; // 스레드 ID

    private UserSummaryResponse otherUser; // 대화 상대방 정보

    private MessageResponse lastMessage; // 마지막 메시지

    private Integer unreadCount; // 읽지 않은 메시지 수

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageAt; // 마지막 메시지 시간

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt; // 스레드 생성 시간
}
