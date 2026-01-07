package com.community.platform.messaging.dto;

import com.community.platform.messaging.domain.MessageStatus;
import com.community.platform.user.dto.UserSummaryResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 메시지 응답 DTO
 */
@Getter
@Builder
public class MessageResponse {

    private Long id; // 메시지 ID

    private Long threadId; // 대화 스레드 ID

    private UserSummaryResponse sender; // 발신자 정보

    private UserSummaryResponse recipient; // 수신자 정보

    private String content; // 메시지 내용

    private MessageStatus status; // 메시지 상태

    private Boolean isRead; // 읽음 여부

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readAt; // 읽은 시간

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt; // 전송 시간

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt; // 수정 시간

    // 사용자별 정보
    private Boolean isSender; // 발신자 여부
}
